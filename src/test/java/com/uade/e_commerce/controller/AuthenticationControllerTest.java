package com.uade.e_commerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.uade.e_commerce.exception.EmailAlreadyExistsException;
import com.uade.e_commerce.exception.InvalidCredentialsException;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.service.AuthenticationService;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    private User buildUser() {
        return new User(1L, "Ada", "Lovelace", "ada@test.com", "hash", "L1", LocalDateTime.now(), true);
    }

    @Test
    void register_validData_returnsOk() throws Exception {
        when(authenticationService.register(any(User.class))).thenReturn(buildUser());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"email\":\"ada@test.com\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@test.com"));
    }

    @Test
    void register_emailAlreadyExists_returns409() throws Exception {
        when(authenticationService.register(any(User.class)))
                .thenThrow(new EmailAlreadyExistsException("ada@test.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ada\",\"email\":\"ada@test.com\",\"password\":\"secret\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void login_validCredentials_returnsOk() throws Exception {
        when(authenticationService.authenticate("ada@test.com", "secret")).thenReturn(buildUser());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ada@test.com\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@test.com"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(authenticationService.authenticate("ada@test.com", "wrong"))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ada@test.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
