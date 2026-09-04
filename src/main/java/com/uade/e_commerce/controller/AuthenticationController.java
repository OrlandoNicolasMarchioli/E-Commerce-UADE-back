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
// El alta de usuario vive acá y no en /api/users porque registrarse es parte de
// la autenticación. Lo que queda en /api/users es el ABM administrativo.
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
    // Si las credenciales no sirven, el service tira InvalidCredentialsException
    // y el GlobalExceptionHandler la convierte en un 401. Por eso acá no hay
    // ningún if: si llegamos a la siguiente línea, el usuario es válido.
    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        User user = authenticationService.authenticate(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
        return ResponseEntity.ok(UserResponseDTO.fromEntity(user));
    }
}
