package com.altis.library.config;

import com.altis.library.auth.filters.JwtAuthenticationFilter;
import com.altis.library.auth.services.JwtService;
import com.altis.library.exceptions.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.time.LocalDateTime;

// é a classe que configura a segurança da API: JWT, autenticação, permissões por rota e respostas de erro
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // guarda o filtro que verifica o JWT das requisições

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) { // injeta o filtro pelo construtor
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public static JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // faz o filtro JWT ser executado antes do filtro padrão de autenticação do Spring
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, exception) -> // Define o que acontece quando alguém tenta acessar uma rota protegida sem autenticação válida
                                writeSecurityError(
                                        response,
                                        objectMapper,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Unauthorized",
                                        "Token de autenticação inválido ou expirado.", // 401
                                        request.getRequestURI()
                                ))
                        .accessDeniedHandler((request, response, exception) ->
                                writeSecurityError(
                                        response,
                                        objectMapper,
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "Forbidden",
                                        "Acesso negado.", // 403
                                        request.getRequestURI()
                                )))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/auth/password-recovery/verify",
                                "/auth/password-recovery/reset"
                        ).permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/books/**").hasRole("ADMIN")
                        .requestMatchers("/publishers/**").hasRole("ADMIN")
                        .requestMatchers("/dashboard").hasRole("ADMIN")
                        .requestMatchers("/loans/my").hasRole("USER")
                        .requestMatchers("/loans/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    private void writeSecurityError( //esse método evita repetir código para os erros
            HttpServletResponse response,
            ObjectMapper objectMapper,
            int status,
            String error,
            String message,
            String path) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path
        );

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}