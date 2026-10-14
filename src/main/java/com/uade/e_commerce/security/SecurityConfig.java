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

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http
    ) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)

            // Authentication is validated through JWT on each request.
            // HTTP sessions are disabled to keep the API stateless.
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/login"
                ).permitAll()

                // Customers can browse products and categories
                // without creating an account or logging in.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/products",
                    "/api/products/**"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/category",
                    "/api/category/*"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/users"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/category"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/category/*"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/category/*"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/products"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/products/*"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/products/*"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/products/*/images"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/products/*/images/*"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/orders/*/status"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/users/*"
                ).authenticated()

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/users/*"
                ).authenticated()

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/users/*"
                ).authenticated()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/products/*/reviews"
                ).authenticated()

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/products/*/reviews/*"
                ).authenticated()

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/products/*/reviews/*"
                ).authenticated()

                // Cart operations require authentication because
                // the cart belongs to a specific user.
                .requestMatchers(
                    "/api/cart",
                    "/api/cart/**"
                ).authenticated()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/orders"
                ).authenticated()

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/orders",
                    "/api/orders/*"
                ).authenticated()

                // New endpoints must not become public accidentally
                // when their access rules have not been defined yet.
                .anyRequest().authenticated()
            )

            .exceptionHandling(ex -> ex

                .authenticationEntryPoint(
                    (request, response, exception) -> {

                        response.setStatus(401);
                        response.setContentType(
                            "application/json;charset=UTF-8"
                        );

                        response.getWriter().write(
                            "{\"status\":401," +
                            "\"error\":\"Unauthorized\"," +
                            "\"message\":\"Se requiere autenticación\"}"
                        );
                    }
                )

                .accessDeniedHandler(
                    (request, response, exception) -> {

                        response.setStatus(403);
                        response.setContentType(
                            "application/json;charset=UTF-8"
                        );

                        response.getWriter().write(
                            "{\"status\":403," +
                            "\"error\":\"Forbidden\"," +
                            "\"message\":\"No tenés permisos para acceder a este recurso\"}"
                        );
                    }
                )
            )

            // The JWT filter must run before Spring Security
            // processes username and password authentication.
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}