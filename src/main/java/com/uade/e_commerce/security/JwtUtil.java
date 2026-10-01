package com.uade.e_commerce.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.uade.e_commerce.exception.InvalidJwtTokenException;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;

/**
 * JWT utility. Spring creates a single instance and injects it where needed.
 */
@Component
public class JwtUtil {

    /**
     * Secret key used to sign JWT tokens. Injected from application.properties.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token lifetime in milliseconds. Injected from application.properties.
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Builds the HMAC signing key from the configured secret (min 256 bits for
     * HS256).
     *
     * @return the secret signing key.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generates a signed JWT containing the username (subject), roles,
     * issued-at and expiration timestamps. Called from AuthenticationService on
     * login.
     *
     * @param username the user's name.
     * @param roles the user's roles.
     * @return the signed JWT as a string.
     */
    public String generateToken(String username, Set<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", String.join(",", roles))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extracts the username (subject) from the token. Called from JwtFilter.
     *
     * @param token the JWT.
     * @return the username.
     */
    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extracts the user's roles from the token. Called from JwtFilter.
     *
     * @param token the JWT.
     * @return the user's roles.
     */
    public Set<String> getRoles(String token) {
        String roles = (String) getClaims(token).get("roles");
        return Set.of(roles.split(","));
    }

    /**
     * Validates the token (signature + expiration). Returns false if expired or
     * tampered. Called from JwtFilter before authenticating the user.
     *
     * @param token the JWT to validate.
     * @return true if valid, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            throw new InvalidJwtTokenException("Token expired", e);
        } catch (JwtException e) {
            throw new InvalidJwtTokenException("Invalid token", e);
        }
    }

    /**
     * Parses the token, verifies its signature and returns the claims. Throws
     * an exception if the token is malformed, expired, or has an invalid
     * signature.
     *
     * @param token the JWT.
     * @return the token's claims.
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
