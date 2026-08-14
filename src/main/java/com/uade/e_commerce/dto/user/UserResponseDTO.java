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
    private String nombre;
    private String apellido;
    private String email;
    private String legajo;
    private LocalDateTime fechaRegistro;
    private boolean habilitado;

    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getNombre(),
                user.getApellido(),
                user.getEmail(),
                user.getLegajo(),
                user.getFechaRegistro(),
                user.isHabilitado());
    }
}
