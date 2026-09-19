package api_inovacao.service;

import api_inovacao.dto.UsuarioResponseDTO;
import api_inovacao.model.Role;
import api_inovacao.model.User;
import api_inovacao.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    // Quantidade de pontos que o Operador ganha quando uma ideia sua é aprovada
    public static final int PONTOS_POR_IDEIA_APROVADA = 100;

    @Autowired
    private UserRepository repository;

    // O principal autenticado já vem fresco do banco a cada requisição (ver SecurityFilter),
    // então não precisa buscar de novo aqui.
    public UsuarioResponseDTO meuPerfil(User usuarioLogado) {
        return new UsuarioResponseDTO(usuarioLogado);
    }

    // Ranking de Inovação: só Operadores pontuam, então só eles aparecem no ranking
    public List<UsuarioResponseDTO> ranking() {
        return repository.findTop5ByRoleOrderByPontosDesc(Role.OPERADOR).stream()
                .map(UsuarioResponseDTO::new)
                .toList();
    }

    // Chamado pelo IdeiaService quando o Gestor aprova uma ideia
    public void adicionarPontos(String email, int quantidade) {
        repository.findByEmail(email).ifPresent(user -> {
            user.setPontos(user.getPontos() + quantidade);
            repository.save(user);
        });
    }
}
