package api_inovacao.exception;

import api_inovacao.dto.ErroResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

// Captura as exceções de TODOS os controllers e devolve sempre o mesmo formato de JSON
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Erros de regra de negócio lançados pelos services (404, 409, 422...)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponseDTO> tratarRegraDeNegocio(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return montar(status, ex.getReason(), request);
    }

    // Erros do @Valid: junta todos os campos inválidos numa mensagem só
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return montar(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    // JSON malformado ou valor inválido de enum no corpo (ex.: "status": "Aprovada")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarJsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, "JSON inválido ou valor não aceito para algum campo", request);
    }

    // Valor inválido em parâmetro da URL (ex.: ?status=XPTO)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarParametroInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + ex.getName() + "'", request);
    }

    // Login com e-mail ou senha errados: antes devolvia 403, o certo é 401
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ErroResponseDTO> tratarCredenciais(AuthenticationException ex, HttpServletRequest request) {
        return montar(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos", request);
    }

    // Rede de segurança: qualquer erro não previsto vira 500 com mensagem genérica
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> tratarErroInesperado(Exception ex, HttpServletRequest request) {
        ex.printStackTrace(); // aparece no console para o time investigar
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado: " + ex.getClass().getSimpleName(), request);
    }

    private ResponseEntity<ErroResponseDTO> montar(HttpStatus status, String mensagem, HttpServletRequest request) {
        ErroResponseDTO corpo = new ErroResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem == null ? status.getReasonPhrase() : mensagem,
                request.getRequestURI());
        return ResponseEntity.status(status).body(corpo);
    }
}
