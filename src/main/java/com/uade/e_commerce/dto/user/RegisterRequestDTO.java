package com.uade.e_commerce.dto.user;

import com.uade.e_commerce.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Registration has its own DTO so its requirements can evolve
// independently from the administrative user update endpoint.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String regNumber;

    public User toEntity() {

        // Only registration fields are accepted from the client.
        // The role, account status and registration date retain
        // the defaults defined by the User model.
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(password)
                .regNumber(regNumber)
                .build();
    }
}