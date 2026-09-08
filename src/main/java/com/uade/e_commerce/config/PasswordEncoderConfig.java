package com.uade.e_commerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// The encoder is declared as a bean so it can be injected by constructor
// into the services, just like the repositories, and so the algorithm can
// be changed by touching a single place if needed down the line.
//
// Important: there is NO Spring Security configuration here. The project
// only depends on spring-security-crypto (the hashing module), not the
// full starter, so there's no security filter and all endpoints remain
// public like before.
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // The no-args constructor uses BCrypt with strength 10, which is
        // the recommended default value.
        return new BCryptPasswordEncoder();
    }
}
