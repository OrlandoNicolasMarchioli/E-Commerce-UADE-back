package com.uade.e_commerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uade.e_commerce.dto.user.AuthResponseDTO;
import com.uade.e_commerce.dto.user.LoginRequestDTO;
import com.uade.e_commerce.dto.user.RegisterRequestDTO;
import com.uade.e_commerce.exception.EmailAlreadyExistsException;
import com.uade.e_commerce.exception.InvalidCredentialsException;
import com.uade.e_commerce.model.Role;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.UserRepository;
import com.uade.e_commerce.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User enabledUser;

    @BeforeEach
    void setUp() {
        enabledUser = new User(
                1L,
                "Ada",
                "Lovelace",
                "ada@test.com",
                "encodedHash",
                "L1",
                LocalDateTime.now(),
                true,
                Role.USER
        );
    }

    @Test
    void register_emailNotTaken_encodesPasswordAndGeneratesToken() {

        RegisterRequestDTO request = new RegisterRequestDTO(
                "Ada",
                "Lovelace",
                "ada@test.com",
                "rawPassword",
                "L1"
        );

        when(userRepository.existsByEmail("ada@test.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("rawPassword"))
                .thenReturn("encodedHash");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtService.generateToken(any(User.class)))
                .thenReturn("test-token");

        AuthResponseDTO result = authenticationService.register(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        assertThat(userCaptor.getValue().getPassword())
                .isEqualTo("encodedHash");

        assertThat(result.getToken())
                .isEqualTo("test-token");

        assertThat(result.getUser().getEmail())
                .isEqualTo("ada@test.com");

        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void register_emailAlreadyTaken_throwsAndDoesNotSave() {

        RegisterRequestDTO request = new RegisterRequestDTO(
                "Ada",
                "Lovelace",
                "ada@test.com",
                "rawPassword",
                "L1"
        );

        when(userRepository.existsByEmail("ada@test.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_correctCredentials_generatesToken() {

        LoginRequestDTO request = new LoginRequestDTO(
                "ada@test.com",
                "rawPassword"
        );

        when(userRepository.findByEmail("ada@test.com"))
                .thenReturn(Optional.of(enabledUser));

        when(passwordEncoder.matches("rawPassword", "encodedHash"))
                .thenReturn(true);

        when(jwtService.generateToken(enabledUser))
                .thenReturn("test-token");

        AuthResponseDTO result = authenticationService.authenticate(request);

        assertThat(result.getToken())
                .isEqualTo("test-token");

        assertThat(result.getUser().getEmail())
                .isEqualTo("ada@test.com");

        verify(jwtService).generateToken(enabledUser);
    }

    @Test
    void authenticate_userDoesNotExist_throwsInvalidCredentials() {

        LoginRequestDTO request = new LoginRequestDTO(
                "nobody@test.com",
                "any"
        );

        when(userRepository.findByEmail("nobody@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authenticate(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_disabledUser_throwsInvalidCredentials() {

        enabledUser.setEnabled(false);

        LoginRequestDTO request = new LoginRequestDTO(
                "ada@test.com",
                "rawPassword"
        );

        when(userRepository.findByEmail("ada@test.com"))
                .thenReturn(Optional.of(enabledUser));

        assertThatThrownBy(() -> authenticationService.authenticate(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_blankPassword_throwsInvalidCredentials() {

        LoginRequestDTO request = new LoginRequestDTO(
                "ada@test.com",
                "  "
        );

        when(userRepository.findByEmail("ada@test.com"))
                .thenReturn(Optional.of(enabledUser));

        assertThatThrownBy(() -> authenticationService.authenticate(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_wrongPassword_throwsInvalidCredentials() {

        LoginRequestDTO request = new LoginRequestDTO(
                "ada@test.com",
                "wrongPassword"
        );

        when(userRepository.findByEmail("ada@test.com"))
                .thenReturn(Optional.of(enabledUser));

        when(passwordEncoder.matches("wrongPassword", "encodedHash"))
                .thenReturn(false);

        assertThatThrownBy(() -> authenticationService.authenticate(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }
}