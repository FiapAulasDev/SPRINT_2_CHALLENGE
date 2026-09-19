package api_inovacao.dto;

import api_inovacao.model.Role;
import api_inovacao.model.User;

// Perfil do usuário logado e itens do ranking de gamificação (nunca expõe a senha)
public record UsuarioResponseDTO(
        String email,
        String nome,
        Role role,
        int pontos
) {
    public UsuarioResponseDTO(User user) {
        this(user.getEmail(), user.getNome(), user.getRole(), user.getPontos());
    }
}
