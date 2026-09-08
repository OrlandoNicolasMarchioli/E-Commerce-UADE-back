package com.uade.e_commerce.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.user.LoginRequestDTO;
import com.uade.e_commerce.dto.user.RegisterRequestDTO;
import com.uade.e_commerce.dto.user.UserResponseDTO;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.service.AuthenticationService;

// http://localhost:8080/api/auth
//
// User sign-up lives here and not in /api/users because registering is
// part of authentication. What's left in /api/users is the administrative
// CRUD.
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // post http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        User registeredUser = authenticationService.register(registerRequestDTO.toEntity());
        return ResponseEntity.ok(UserResponseDTO.fromEntity(registeredUser));
    }

    // post http://localhost:8080/api/auth/login
    // If the credentials aren't valid, the service throws
    // InvalidCredentialsException and the GlobalExceptionHandler turns it
    // into a 401. That's why there's no if here: if we reach the next
    // line, the user is valid.
    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        User user = authenticationService.authenticate(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));
    }
}
