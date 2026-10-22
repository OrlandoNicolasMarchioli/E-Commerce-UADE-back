package com.uade.e_commerce.dto.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.uade.e_commerce.model.Role;
import com.uade.e_commerce.model.User;

class RegisterRequestDTOTest {

    @Test
    void toEntity_buildsUserWithRegistrationData() {

        RegisterRequestDTO request = new RegisterRequestDTO(
                "Ada",
                "Lovelace",
                "ada@test.com",
                "rawPassword",
                "L1"
        );

        User user = request.toEntity();

        assertThat(user.getFirstName()).isEqualTo("Ada");
        assertThat(user.getLastName()).isEqualTo("Lovelace");
        assertThat(user.getEmail()).isEqualTo("ada@test.com");
        assertThat(user.getPassword()).isEqualTo("rawPassword");
        assertThat(user.getRegNumber()).isEqualTo("L1");
        assertThat(user.getId()).isNull();
    }

    @Test
    void toEntity_preservesDefaultUserValues() {

        RegisterRequestDTO request = new RegisterRequestDTO(
                "Ada",
                "Lovelace",
                "ada@test.com",
                "rawPassword",
                "L1"
        );

        User user = request.toEntity();

        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getRegistrationDate()).isNotNull();
    }

    @Test
    void noArgsConstructor_preservesDefaultUserValues() {

        User user = new User();

        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getRegistrationDate()).isNotNull();
    }
}