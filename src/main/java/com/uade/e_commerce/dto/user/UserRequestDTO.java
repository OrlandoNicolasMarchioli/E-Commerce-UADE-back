
package com.uade.e_commerce.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// This DTO contains only the fields supported by the user update API.
// Account roles and enabled status are not controlled by this request.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String regNumber;
}
