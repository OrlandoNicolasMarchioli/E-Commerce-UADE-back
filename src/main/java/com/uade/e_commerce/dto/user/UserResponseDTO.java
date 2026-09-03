package com.uade.e_commerce.dto.user;

import java.time.LocalDateTime;

import com.uade.e_commerce.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String regNumber; //legajo
    private LocalDateTime registrationDate;
    private boolean IsEnabled;

    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRegNumber(),
                user.getRegistrationDate(),
                user.isEnabled());
    }
}
