package api_inovacao.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.LocalDateTime;

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

                        // Perfil do usuário logado e ranking de gamificação: qualquer perfil autenticado lê
                        .requestMatchers("/api/usuarios/**").authenticated()

                        // Regras de negócio da Etapa 1 (Orientações Estratégicas)
                        // "/**" cobre também /api/estrategias/{id} e /api/estrategias/vigente
                        .requestMatchers(HttpMethod.GET, "/api/estrategias/**").authenticated()
                        .requestMatchers("/api/estrategias/**").hasRole("LIDER")

                        // Regras da Etapa 2 - Parte 1 (Ideias). A ORDEM IMPORTA: vale a primeira regra que casar
                        // 1) qualquer usuário logado pode ler as ideias
                        .requestMatchers(HttpMethod.GET, "/api/ideias/**").authenticated()
                        // 2) aprovar/rejeitar é exclusivo do Gestor ("*" casa só um trecho da URL)
                        .requestMatchers(HttpMethod.PATCH, "/api/ideias/*/avaliacao").hasRole("GESTOR")
                        // 3) pedir a análise da IA também é do Gestor (é ele quem prioriza)
                        .requestMatchers(HttpMethod.POST, "/api/ideias/*/analise-ia").hasRole("GESTOR")
                        // 4) o resto (POST, PUT, DELETE) é do Operador, o dono da ideia
                        .requestMatchers("/api/ideias/**").hasRole("OPERADOR")

                        // Etapa 2 - Parte 2 (Projetos): todos leem, só o Gestor cadastra e atualiza
                        .requestMatchers(HttpMethod.GET, "/api/projetos/**").authenticated()
                        .requestMatchers("/api/projetos/**").hasRole("GESTOR")

                        // Etapa 2 - Parte 4 (Dashboard): painel de acompanhamento do Líder
                        .requestMatchers("/api/dashboard/**").hasRole("LIDER")

                        // Bloqueia qualquer outra rota não mapeada
                        .anyRequest().authenticated()
                )
                // Erros de token/permissão também saem no mesmo formato JSON do GlobalExceptionHandler
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, ex) ->
                                escreverErro(request, response, HttpStatus.UNAUTHORIZED, "Token ausente ou inválido"))
                        .accessDeniedHandler((request, response, ex) ->
                                escreverErro(request, response, HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para esta operação"))
                )
                // Coloca o nosso filtro de JWT ANTES do filtro padrão do Spring
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // Esses erros acontecem ANTES do controller, por isso não passam pelo @RestControllerAdvice
    private void escreverErro(HttpServletRequest request, HttpServletResponse response,
                              HttpStatus status, String mensagem) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
                {"timestamp":"%s","status":%d,"erro":"%s","mensagem":"%s","caminho":"%s"}"""
                .formatted(LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensagem, request.getRequestURI()));
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