package com.uade.e_commerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Se declara el encoder como bean para poder inyectarlo por constructor en los
// services, igual que los repositories, y para que el día de mañana se pueda
// cambiar el algoritmo tocando un solo lugar.
//
// Importante: acá NO hay ninguna configuración de Spring Security. El proyecto
// depende solo de spring-security-crypto (el módulo de hashing), no del starter
// completo, así que no existe filtro de seguridad y todos los endpoints siguen
// siendo públicos como hasta ahora.
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // El constructor sin parámetros usa BCrypt con strength 10, que es el
        // valor recomendado por defecto.
        return new BCryptPasswordEncoder();
    }
}
