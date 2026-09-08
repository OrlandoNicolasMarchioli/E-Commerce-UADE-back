package com.uade.e_commerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uade.e_commerce.exception.EmailAlreadyExistsException;
import com.uade.e_commerce.exception.InvalidCredentialsException;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User enabledUser;

    @BeforeEach
    void setUp() {
        enabledUser = new User(1L, "Ada", "Lovelace", "ada@test.com", "encodedHash", "L1", LocalDateTime.now(), true);
    }

    @Test
    void register_emailNotTaken_encodesPasswordAndSaves() {
        User toRegister = new User(null, "Ada", "Lovelace", "ada@test.com", "rawPassword", "L1", null, true);
        when(userRepository.existsByEmail("ada@test.com")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authenticationService.register(toRegister);

        assertThat(result.getPassword()).isEqualTo("encodedHash");
    }

    @Test
    void register_emailAlreadyTaken_throwsAndDoesNotSave() {
        User toRegister = new User(null, "Ada", "Lovelace", "ada@test.com", "rawPassword", "L1", null, true);
        when(userRepository.existsByEmail("ada@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(toRegister))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_correctCredentials_returnsUser() {
        when(userRepository.findByEmail("ada@test.com")).thenReturn(Optional.of(enabledUser));
        when(passwordEncoder.matches("rawPassword", "encodedHash")).thenReturn(true);

        User result = authenticationService.authenticate("ada@test.com", "rawPassword");

        assertThat(result).isEqualTo(enabledUser);
    }

    @Test
    void authenticate_userDoesNotExist_throwsInvalidCredentials() {
        when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authenticate("nobody@test.com", "any"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void authenticate_disabledUser_throwsInvalidCredentials() {
        enabledUser.setEnabled(false);
        when(userRepository.findByEmail("ada@test.com")).thenReturn(Optional.of(enabledUser));

        assertThatThrownBy(() -> authenticationService.authenticate("ada@test.com", "rawPassword"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void authenticate_blankPassword_throwsInvalidCredentialsWithoutCallingEncoder() {
        lenient().when(userRepository.findByEmail("ada@test.com")).thenReturn(Optional.of(enabledUser));

        assertThatThrownBy(() -> authenticationService.authenticate("ada@test.com", "  "))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void authenticate_wrongPassword_throwsInvalidCredentials() {
        when(userRepository.findByEmail("ada@test.com")).thenReturn(Optional.of(enabledUser));
        when(passwordEncoder.matches("wrongPassword", "encodedHash")).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.authenticate("ada@test.com", "wrongPassword"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
