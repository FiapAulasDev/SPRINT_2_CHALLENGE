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
            criarUsuarioSeNaoExistir(userRepository, passwordEncoder, "lider@aguiabranca.com", "Líder Águia Branca", Role.LIDER);
            criarUsuarioSeNaoExistir(userRepository, passwordEncoder, "gestor@aguiabranca.com", "Gestor Águia Branca", Role.GESTOR);
            criarUsuarioSeNaoExistir(userRepository, passwordEncoder, "operador@aguiabranca.com", "Operador Águia Branca", Role.OPERADOR);
        };
    }

    private void criarUsuarioSeNaoExistir(UserRepository userRepository, PasswordEncoder passwordEncoder, String email, String nome, Role role) {
        var existente = userRepository.findByEmail(email);

        if (existente.isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setNome(nome);
            // A senha vai para o banco criptografada!
            user.setSenha(passwordEncoder.encode("123456"));
            user.setRole(role);
            user.setPontos(0);

            userRepository.save(user);
            System.out.println("========== Usuário " + role + " criado com sucesso no MongoDB Atlas: " + email + " ==========");
        } else if (existente.get().getNome() == null || existente.get().getNome().isBlank()) {
            // Backfill: bancos criados antes do campo "nome" existir (ex.: Atlas compartilhado do time)
            User user = existente.get();
            user.setNome(nome);
            userRepository.save(user);
        }
    }
}