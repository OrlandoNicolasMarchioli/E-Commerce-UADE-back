
package com.uade.e_commerce.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.user.UserRequestDTO;
import com.uade.e_commerce.dto.user.UserResponseDTO;
import com.uade.e_commerce.exception.EmailAlreadyExistsException;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    public UserResponseDTO getUserById(Long id) {
        User user = findUserById(id);
        return UserResponseDTO.fromEntity(user);
    }

    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        User existingUser = findUserById(id);

        // Keeping the current email is valid when updating other fields.
        // Only a different email needs a uniqueness check.
        if (!existingUser.getEmail().equals(dto.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(dto.getEmail());
        }

        existingUser.setFirstName(dto.getFirstName());
        existingUser.setLastName(dto.getLastName());
        existingUser.setEmail(dto.getEmail());
        existingUser.setRegNumber(dto.getRegNumber());

        // An omitted or blank password must not replace the stored hash.
        // A new password is encoded before the entity is persisted.
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existingUser.setPassword(
                    passwordEncoder.encode(dto.getPassword())
            );
        }

        // Role and account status are intentionally not modified.
        // They are not editable through UserRequestDTO.
        User updated = userRepository.save(existingUser);

        return UserResponseDTO.fromEntity(updated);
    }

    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(
                    "Usuario no encontrado con id: " + id
            );
        }

        userRepository.deleteById(id);
        return true;
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Usuario no encontrado con id: " + id
                        )
                );
    }
}
