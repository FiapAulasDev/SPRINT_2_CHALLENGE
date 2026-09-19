package api_inovacao;

import api_inovacao.model.Role;
import api_inovacao.model.User;
import api_inovacao.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;


@SpringBootApplication
public class ApiInovacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiInovacaoApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Verifica por e-mail (e não por count) para criar os usuários que faltarem
            criarUsuarioSeNaoExistir(userRepository, passwordEncoder, "lider@aguiabranca.com", Role.LIDER);
            criarUsuarioSeNaoExistir(userRepository, passwordEncoder, "gestor@aguiabranca.com", Role.GESTOR);
            criarUsuarioSeNaoExistir(userRepository, passwordEncoder, "operador@aguiabranca.com", Role.OPERADOR);
        };
    }

    private void criarUsuarioSeNaoExistir(UserRepository userRepository, PasswordEncoder passwordEncoder, String email, Role role) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setEmail(email);
            // A senha vai para o banco criptografada!
            user.setSenha(passwordEncoder.encode("123456"));
            user.setRole(role);

            userRepository.save(user);
            System.out.println("========== Usuário " + role + " criado com sucesso no MongoDB Atlas: " + email + " ==========");
        }
    }
}