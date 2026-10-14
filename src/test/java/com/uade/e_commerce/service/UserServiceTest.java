
package com.uade.e_commerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
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

import com.uade.e_commerce.dto.user.UserRequestDTO;
import com.uade.e_commerce.dto.user.UserResponseDTO;
import com.uade.e_commerce.exception.EmailAlreadyExistsException;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.Role;
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
        return new User(
                id,
                "Ada",
                "Lovelace",
                email,
                password,
                "L1",
                LocalDateTime.now(),
                true,
                Role.USER
        );
    }

    private UserRequestDTO buildRequest(String email, String password) {
        return new UserRequestDTO(
                "Ada",
                "Lovelace",
                email,
                password,
                "L1"
        );
    }

    @Test
    void getAllUsers_returnsDTOList() {
        User user = buildUser(1L, "ada@test.com", "hash");

        when(userRepository.findAll())
                .thenReturn(List.of(user));

        List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getEmail()).isEqualTo("ada@test.com");
    }

    @Test
    void getUserById_found_returnsDTO() {
        User user = buildUser(1L, "ada@test.com", "hash");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponseDTO result = userService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("ada@test.com");
    }

    @Test
    void getUserById_notFound_throwsException() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUser_sameEmail_updatesFieldsAndKeepsPassword() {
        User existing = buildUser(1L, "ada@test.com", "oldHash");

        UserRequestDTO request =
                buildRequest("ada@test.com", null);

        request.setFirstName("Grace");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO result = userService.updateUser(1L, request);

        assertThat(result.getFirstName()).isEqualTo("Grace");
        assertThat(existing.getPassword()).isEqualTo("oldHash");

        verify(userRepository, never()).existsByEmail(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUser_newEmailAvailable_updatesEmail() {
        User existing = buildUser(1L, "old@test.com", "oldHash");

        UserRequestDTO request =
                buildRequest("new@test.com", null);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(userRepository.existsByEmail("new@test.com"))
                .thenReturn(false);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO result = userService.updateUser(1L, request);

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(existing.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void updateUser_newEmailAlreadyTaken_throwsException() {
        User existing = buildUser(1L, "old@test.com", "oldHash");

        UserRequestDTO request =
                buildRequest("taken@test.com", null);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(userRepository.existsByEmail("taken@test.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_notFound_throwsException() {
        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.updateUser(
                        99L,
                        buildRequest("ada@test.com", null)
                )
        ).isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_blankPassword_keepsOldHash() {
        User existing = buildUser(1L, "ada@test.com", "oldHash");

        UserRequestDTO request =
                buildRequest("ada@test.com", " ");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(1L, request);

        assertThat(existing.getPassword()).isEqualTo("oldHash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUser_nullPassword_keepsOldHash() {
        User existing = buildUser(1L, "ada@test.com", "oldHash");

        UserRequestDTO request =
                buildRequest("ada@test.com", null);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(1L, request);

        assertThat(existing.getPassword()).isEqualTo("oldHash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void updateUser_newPassword_encodesBeforeSaving() {
        User existing = buildUser(1L, "ada@test.com", "oldHash");

        UserRequestDTO request =
                buildRequest("ada@test.com", "newRawPassword");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(passwordEncoder.encode("newRawPassword"))
                .thenReturn("newHash");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO result = userService.updateUser(1L, request);

        assertThat(existing.getPassword()).isEqualTo("newHash");
        assertThat(result.getEmail()).isEqualTo("ada@test.com");

        verify(passwordEncoder).encode("newRawPassword");
        verify(userRepository).save(existing);
    }

    @Test
    void updateUser_disabledAccount_remainsDisabled() {
        User existing = buildUser(1L, "ada@test.com", "oldHash");
        existing.setEnabled(false);

        UserRequestDTO request =
                buildRequest("ada@test.com", null);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateUser(1L, request);

        assertThat(existing.isEnabled()).isFalse();
        assertThat(existing.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void deleteUser_found_deletesAndReturnsTrue() {
        when(userRepository.existsById(1L))
                .thenReturn(true);

        boolean result = userService.deleteUser(1L);

        assertThat(result).isTrue();
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notFound_throwsException() {
        when(userRepository.existsById(anyLong()))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).deleteById(anyLong());
    }
}
