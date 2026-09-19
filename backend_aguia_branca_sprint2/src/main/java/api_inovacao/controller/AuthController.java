package api_inovacao.controller;

import api_inovacao.dto.AuthenticationDTO;
import api_inovacao.dto.LoginResponseDTO;
import api_inovacao.dto.RegisterRequestDTO;
import api_inovacao.dto.UsuarioResponseDTO;
import api_inovacao.model.User;
import api_inovacao.security.TokenService;
import api_inovacao.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody AuthenticationDTO data) {
        // Pega o e-mail e senha enviados na requisição e cria um token interno do Spring
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.senha());

        // O Spring Security vai até o banco, busca o usuário e valida se a senha bate
        var auth = this.authenticationManager.authenticate(usernamePassword);

        // Se a senha estiver correta, geramos o nosso JWT
        var token = tokenService.gerarToken((User) auth.getPrincipal());

        // Devolvemos o token na resposta
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody RegisterRequestDTO dados) {
        User usuario = usuarioService.cadastrar(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioResponseDTO(usuario));
    }
}
