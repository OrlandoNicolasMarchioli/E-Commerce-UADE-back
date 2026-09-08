package com.uade.e_commerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uade.e_commerce.exception.EmailAlreadyExistsException;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User buildUser(Long id, String email, String password) {
        return new User(id, "Ada", "Lovelace", email, password, "L1", LocalDateTime.now(), true);
    }

    @Test
    void getAllUsers_returnsWhatTheRepositoryReturns() {
        User user = buildUser(1L, "ada@test.com", "hash");
        when(userRepository.findAll()).thenReturn(List.of(user));

        assertThat(userService.getAllUsers()).containsExactly(user);
    }

    @Test
    void getUserById_found_returnsUser() {
        User user = buildUser(1L, "ada@test.com", "hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(userService.getUserById(1L)).isEqualTo(user);
    }

    @Test
    void getUserById_notFound_throwsUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUser_sameEmail_doesNotCheckDuplicateAndUpdatesFields() {
        User existing = buildUser(1L, "ada@test.com", "oldHash");
        User changes = buildUser(null, "ada@test.com", null);
        changes.setFirstName("Grace");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(1L, changes);

        assertThat(result.getFirstName()).isEqualTo("Grace");
        assertThat(result.getPassword()).isEqualTo("oldHash");
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void updateUser_newEmailAvailable_updatesEmail() {
        User existing = buildUser(1L, "old@test.com", "oldHash");
        User changes = buildUser(null, "new@test.com", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(1L, changes);

        assertThat(result.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void updateUser_newEmailAlreadyTaken_throwsAndDoesNotSave() {
        User existing = buildUser(1L, "old@test.com", "oldHash");
        User changes = buildUser(null, "taken@test.com", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, changes))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_notFound_throwsUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, buildUser(null, "x@test.com", null)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUser_blankPassword_keepsOldPasswordHash() {
        User existing = buildUser(1L, "ada@test.com", "oldHash");
        User changes = buildUser(null, "ada@test.com", "");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(1L, changes);

        assertThat(result.getPassword()).isEqualTo("oldHash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUser_newPassword_encodesBeforeSaving() {
        User existing = buildUser(1L, "ada@test.com", "oldHash");
        User changes = buildUser(null, "ada@test.com", "newRawPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newRawPassword")).thenReturn("newHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(1L, changes);

        assertThat(result.getPassword()).isEqualTo("newHash");
    }

    @Test
    void deleteUser_found_deletesAndReturnsTrue() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThat(userService.deleteUser(1L)).isTrue();
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_notFound_throwsAndDoesNotDelete() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).deleteById(any());
    }
}
