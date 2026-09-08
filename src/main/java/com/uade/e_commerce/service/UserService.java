package com.uade.e_commerce.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.exception.EmailAlreadyExistsException;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository
            .findById(id)
            .orElseThrow(() ->
                new UserNotFoundException("Usuario no encontrado con id: " + id)
            );
    }

    public User updateUser(Long id, User user) {
        User existingUser = userRepository
            .findById(id)
            .orElseThrow(() ->
                new UserNotFoundException("Usuario no encontrado con id: " + id)
            );

        // We compare against the current email so we don't reject an update
        // that sends the same email as always, which is normal when only
        // the name is being edited.
        if (!existingUser.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());

        // Before, it was always overwritten, so an update without a
        // password left the user with a null password and unable to ever
        // log in again.
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        existingUser.setRegNumber(user.getRegNumber());
        existingUser.setEnabled(user.isEnabled());

        return userRepository.save(existingUser);
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

}
