package com.uade.e_commerce.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth

                // --- Públicos ---
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/category", "/api/category/*").permitAll()

                // --- Solo ADMIN ---
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/category").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/category/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/category/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products/*/images").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/*/images/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/orders/*/status").hasRole("ADMIN")

                // --- Autenticado (cualquier rol) ---
                .requestMatchers(HttpMethod.GET, "/api/users/*").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/users/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/users/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/products/*/reviews").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/products/*/reviews/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/products/*/reviews/*").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/cart").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/cart/items").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/cart/items/*").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/cart").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/orders").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/orders").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/orders/*").authenticated()

                .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Se requiere autenticación\"}"
                    );
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"No tenés permisos para acceder a este recurso\"}"
                    );
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
