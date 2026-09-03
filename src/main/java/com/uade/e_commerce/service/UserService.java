package com.uade.e_commerce.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.uade.e_commerce.exception.EmailAlreadyExistsException;
import com.uade.e_commerce.exception.InvalidCredentialsException;
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
        return userRepository.findById(id).orElse(null);
    }

    public User createUser(User user) {
        // Lo chequeamos a mano para devolver un 409 claro: si dejamos que salte la
        // constraint unique de la base, termina en un 500 poco útil.
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }

        // Solo se persiste el hash, nunca lo que mandó el cliente.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            return null;
        }

        // Comparamos contra el email actual para no rechazar un update que manda el
        // mismo email de siempre, que es lo normal cuando solo se edita el nombre.
        if (!existingUser.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }

        existingUser.setName(user.getName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());

        // Antes se pisaba siempre, así que un update sin password dejaba al usuario
        // con la password en null y sin poder loguearse nunca más.
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        existingUser.setRegNumber(user.getRegNumber());
        existingUser.setEnabled(user.isEnabled());

        return userRepository.save(existingUser);
    }

    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    // Por ahora no genera ningún token: el login solo confirma la identidad.
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email).orElse(null);

        // Los tres casos de error tiran la misma excepción a propósito (ver el
        // comentario en InvalidCredentialsException).
        if (user == null || !user.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        // Lo chequeamos nosotros para no depender de cómo trate los nulos la
        // versión de turno del encoder.
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

}
