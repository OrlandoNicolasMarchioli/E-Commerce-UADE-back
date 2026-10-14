package com.uade.e_commerce.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.user.AuthResponseDTO;
import com.uade.e_commerce.dto.user.LoginRequestDTO;
import com.uade.e_commerce.dto.user.RegisterRequestDTO;
import com.uade.e_commerce.dto.user.UserResponseDTO;
import com.uade.e_commerce.exception.EmailAlreadyExistsException;
import com.uade.e_commerce.exception.InvalidCredentialsException;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.UserRepository;
import com.uade.e_commerce.security.JwtService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponseDTO register(RegisterRequestDTO requestDTO) {

        // Checking the email before persistence allows the API to return
        // a clear conflict response instead of a database constraint error.
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new EmailAlreadyExistsException(requestDTO.getEmail());
        }

        User user = requestDTO.toEntity();

        // Plain-text passwords must never be persisted.
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));

        User savedUser = userRepository.save(user);

        return buildAuthResponse(savedUser);
    }

    public AuthResponseDTO authenticate(LoginRequestDTO requestDTO) {

        User user = userRepository.findByEmail(requestDTO.getEmail())
                .orElse(null);

        // Missing, disabled and incorrectly authenticated accounts
        // share the same response to avoid revealing account information.
        if (user == null || !user.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        String rawPassword = requestDTO.getPassword();

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return buildAuthResponse(user);
    }

    private AuthResponseDTO buildAuthResponse(User user) {

        // Token creation belongs to the authentication service,
        // keeping JWT handling outside the controller.
        String token = jwtService.generateToken(user);

        return new AuthResponseDTO(
                token,
                UserResponseDTO.fromEntity(user)
        );
    }
}