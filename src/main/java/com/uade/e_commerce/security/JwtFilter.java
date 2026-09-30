package com.uade.e_commerce.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT authentication filter. Runs before the controller on every request.
 * Configured in SecurityConfig via {@code addFilterBefore}.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private JwtUtil jwtUtil;

    /**
     * Validates the JWT from the Authorization header and populates the
     * SecurityContext with the authenticated user and their roles.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Get header "Authorization" from petition.
        String header = request.getHeader("Authorization");

        // 2. Verify if header exists and starts with "Bearer ".
        if (header != null && header.startsWith("Bearer ")) {
            // extrae la parte del JWT de la cabecera de autorización, eliminando el prefijo "Bearer ".
            String token = header.substring(7);
            // 4. Validates token using 'jwtUtil.validateToken()'.
            if (jwtUtil.validateToken(token)) {
                // 5. If valid, extract username and role.
                String username = jwtUtil.getUsername(token);
                Set<String> roles = jwtUtil.getRoles(token);

                // Convert role strings into Spring Security authorities
                var authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                // usuario ya autenticado , crea un objeto de autenticación con los detalles del usuario y sus roles
                var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                // 8. Finalmente, pasa la petición al siguiente filtro en la cadena.
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
