package com.uade.e_commerce.dto.user;

import com.uade.e_commerce.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Hoy tiene los mismos campos que UserRequestDTO, pero va separado porque el
// alta y la edición de un usuario no tienen por qué pedir lo mismo: cuando se
// sumen validaciones, las del registro van acá sin afectar al PUT de /api/users.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String regNumber; //legajo

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
