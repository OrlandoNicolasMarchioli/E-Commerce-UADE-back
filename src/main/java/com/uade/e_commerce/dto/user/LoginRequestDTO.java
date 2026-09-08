package com.uade.e_commerce.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Unlike the rest of the Request DTOs, this one has no toEntity(): it
// doesn't create or modify a User, it just carries the credentials that get
// validated against the user already stored in the database.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    private String email;
    private String password;
}
