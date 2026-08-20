package com.uade.e_commerce.dto.user;

import com.uade.e_commerce.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String legajo;

    public User toEntity() {
        User user = new User();
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setEmail(email);
        user.setPassword(password);
        user.setLegajo(legajo);
        return user;
    }
}
