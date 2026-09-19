package api_inovacao.security;

import api_inovacao.model.User;
import api_inovacao.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Tenta extrair o token do cabeçalho da requisição
        var token = this.recoverToken(request);

        if (token != null) {
            // 2. Valida o token e extrai o e-mail do usuário
            var email = tokenService.validarToken(token);

            if (!email.isEmpty()) {
                // 3. Busca o usuário no banco de dados MongoDB
                Optional<User> user = userRepository.findByEmail(email);

                if (user.isPresent()) {
                    // 4. Avisa o Spring Security que este usuário está autenticado e passa as Roles dele
                    var authentication = new UsernamePasswordAuthenticationToken(user.get(), null, user.get().getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        // 5. Continua o fluxo da requisição (se não tiver token, barra na porta das rotas privadas)
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        // O padrão JWT exige que a palavra "Bearer " venha antes do token. Aqui nós tiramos ela.
        return authHeader.replace("Bearer ", "");
    }
}