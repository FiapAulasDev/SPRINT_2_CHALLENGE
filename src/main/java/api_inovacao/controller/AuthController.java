package api_inovacao.controller;

import api_inovacao.dto.AuthenticationDTO;
import api_inovacao.dto.LoginResponseDTO;
import api_inovacao.model.User;
import api_inovacao.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

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
}