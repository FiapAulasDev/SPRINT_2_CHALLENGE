package api_inovacao;

import api_inovacao.model.Role;
import api_inovacao.model.User;
import api_inovacao.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@SpringBootApplication
public class ApiInovacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiInovacaoApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User lider = new User();
                lider.setEmail("lider@aguiabranca.com");
                // Agora a senha vai para o banco criptografada!
                lider.setSenha(passwordEncoder.encode("123456"));
                lider.setRole(Role.LIDER);

                userRepository.save(lider);
                System.out.println("========== Usuário LIDER criado com sucesso no MongoDB Atlas! ==========");
            }
        };
    }
}