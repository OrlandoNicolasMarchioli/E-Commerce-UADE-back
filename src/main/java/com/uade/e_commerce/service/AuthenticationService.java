package com.uade.e_commerce.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.exception.EmailAlreadyExistsException;
import com.uade.e_commerce.exception.InvalidCredentialsException;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.UserRepository;

import jakarta.transaction.Transactional;

// El registro y el login viven acá y no en UserService siguiendo la estructura
// de la Clase 05: UserService queda con el ABM administrativo de usuarios y la
// autenticación es su propio servicio.
@Service
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User user) {
        // Lo chequeamos a mano para devolver un 409 claro: si dejamos que salte la
        // constraint unique de la base, termina en un 500 poco útil.
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }

        // Solo se persiste el hash, nunca lo que mandó el cliente.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    // Por ahora no genera ningún token: solo confirma la identidad.
    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email).orElse(null);

        // Los tres casos de error tiran la misma excepción a propósito (ver el
        // comentario en InvalidCredentialsException).
        if (user == null || !user.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        // Lo chequeamos nosotros para no depender de cómo trate los nulos la
        // versión de turno del encoder.
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}
