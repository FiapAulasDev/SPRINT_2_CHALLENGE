package api_inovacao.controller;

import api_inovacao.dto.UsuarioResponseDTO;
import api_inovacao.model.User;
import api_inovacao.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Não existe endpoint de cadastro/edição de usuário: só leitura do próprio perfil e do ranking
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Perfil do usuário logado, incluindo os pontos de gamificação atualizados
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> meuPerfil(@AuthenticationPrincipal User usuarioLogado) {
        return ResponseEntity.ok(usuarioService.meuPerfil(usuarioLogado));
    }

    // Ranking de Inovação: top 5 Operadores por pontos
    @GetMapping("/ranking")
    public ResponseEntity<List<UsuarioResponseDTO>> ranking() {
        return ResponseEntity.ok(usuarioService.ranking());
    }
}
