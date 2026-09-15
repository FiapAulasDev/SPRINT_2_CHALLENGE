package api_inovacao.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable()) // Desativa proteção CSRF, pois a API é stateless (não usa sessão)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Rota de login deve ser pública para qualquer um tentar entrar
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // Rota interna de erro do Spring: sem isso, 404/400 viram 403 sem mensagem
                        .requestMatchers("/error").permitAll()

                        // Regras de negócio da Etapa 1 (Orientações Estratégicas)
                        // "/**" cobre também /api/estrategias/{id} e /api/estrategias/vigente
                        .requestMatchers(HttpMethod.GET, "/api/estrategias/**").authenticated()
                        .requestMatchers("/api/estrategias/**").hasRole("LIDER")

                        // Bloqueia qualquer outra rota não mapeada
                        .anyRequest().authenticated()
                )
                // Coloca o nosso filtro de JWT ANTES do filtro padrão do Spring
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Algoritmo para criptografar as senhas no banco de dados
        return new BCryptPasswordEncoder();
    }
}