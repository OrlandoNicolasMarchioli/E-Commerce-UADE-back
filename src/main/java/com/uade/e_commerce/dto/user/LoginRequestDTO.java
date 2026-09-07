package com.uade.e_commerce.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// A diferencia del resto de los Request DTO, este no tiene toEntity(): no crea
// ni modifica un User, solo transporta las credenciales que se validan contra
// el usuario que ya está guardado en la base.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    private String email;
    private String password;
}
