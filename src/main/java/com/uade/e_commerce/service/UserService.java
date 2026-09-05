package com.uade.e_commerce.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Long id, User user) {
        User existingUser = userRepository
            .findById(id)
            .orElseThrow(() ->
                new UserNotFoundException("Usuario no encontrado con id: " + id)
            );

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPassword(user.getPassword());
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
