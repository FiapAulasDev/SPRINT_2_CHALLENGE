package api_inovacao.repository;

import api_inovacao.model.Role;
import api_inovacao.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    // Esse método será fundamental para o Spring Security encontrar o usuário na hora do Login
    Optional<User> findByEmail(String email);

    // Ranking de Inovação (gamificação): só quem pode ter ideias aprovadas pontua
    List<User> findTop5ByRoleOrderByPontosDesc(Role role);

}