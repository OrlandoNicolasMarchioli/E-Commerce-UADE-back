package com.uade.e_commerce.dto.user;

import com.uade.e_commerce.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Right now it has the same fields as UserRequestDTO, but it's kept
// separate because signing up and editing a user don't necessarily need to
// ask for the same thing: when validations get added, the registration
// ones go here without affecting the PUT on /api/users.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String regNumber; // student id number

    public User toEntity() {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);
        user.setRegNumber(regNumber);
        return user;
    }
}
