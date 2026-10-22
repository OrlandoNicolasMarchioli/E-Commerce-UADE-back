
package com.uade.e_commerce.controller.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.user.UserRequestDTO;
import com.uade.e_commerce.dto.user.UserResponseDTO;
import com.uade.e_commerce.model.Role;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    // Listing all users is restricted to ADMIN by SecurityConfig.
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        if (!canAccessUser(id, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserRequestDTO dto,
            @AuthenticationPrincipal User currentUser) {

        if (!canAccessUser(id, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        if (!canAccessUser(id, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private boolean canAccessUser(Long requestedId, User currentUser) {

        // The JWT filter loads the authenticated user from the database.
        // Never trust a user ID provided in the request as proof of ownership.
        if (currentUser == null || currentUser.getId() == null) {
            return false;
        }

        // Administrators can manage accounts; other users can only
        // access resources associated with their own account.
        return currentUser.getRole() == Role.ADMIN
                || currentUser.getId().equals(requestedId);
    }
}
