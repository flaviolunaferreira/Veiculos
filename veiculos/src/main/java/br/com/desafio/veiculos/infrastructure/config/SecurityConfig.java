package br.com.desafio.veiculos.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // STUB: Em um projeto real, injetaríamos um JwtAuthenticationFilter
    // private final JwtAuthenticationFilter jwtAuthFilter;
    // public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) { this.jwtAuthFilter = jwtAuthFilter; }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desabilitar CSRF para APIs stateless
            .authorizeHttpRequests(auth -> auth
                // Permitir acesso ao Swagger UI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                // Permitir acesso ao Actuator (Health)
                .requestMatchers("/actuator/health").permitAll()
                // DESENVOLVIMENTO: Permitir acesso à API sem autenticação
                // TODO: Em produção, descomentar a linha abaixo para exigir JWT
                // .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().permitAll() // Permitir acesso para testes
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT é stateless
            );
            // .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Adicionar filtro JWT

        return http.build();
    }
}

