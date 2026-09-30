package com.uade.e_commerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.user.AuthResponseDTO;
import com.uade.e_commerce.dto.user.LoginRequestDTO;
import com.uade.e_commerce.dto.user.RegisterRequestDTO;
import com.uade.e_commerce.dto.user.UserResponseDTO;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.security.JwtService;
import com.uade.e_commerce.service.AuthenticationService;

// http://localhost:8080/api/auth
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    AuthenticationController(AuthenticationService authenticationService, JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    // POST http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        User user = authenticationService.register(registerRequestDTO.toEntity());
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDTO(token, UserResponseDTO.fromEntity(user)));
    }

    // POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        User user = authenticationService.authenticate(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDTO(token, UserResponseDTO.fromEntity(user)));
    }
}
