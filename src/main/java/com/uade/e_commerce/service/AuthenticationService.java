package com.uade.e_commerce.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.exception.EmailAlreadyExistsException;
import com.uade.e_commerce.exception.InvalidCredentialsException;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.UserRepository;

import jakarta.transaction.Transactional;

// Registration and login live here instead of in UserService, following the
// structure from Class 05: UserService is left with the administrative
// CRUD of users, and authentication is its own service.
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
        // We check it manually to return a clear 409: if we let the
        // database's unique constraint fire instead, it ends up as a
        // not-very-useful 500.
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }

        // Only the hash gets persisted, never what the client sent.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    // For now it doesn't generate any token: it just confirms identity.
    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email).orElse(null);

        // All three error cases throw the same exception on purpose (see
        // the comment in InvalidCredentialsException).
        if (user == null || !user.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        // We check it ourselves so we don't depend on however the current
        // encoder version happens to handle nulls.
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}
