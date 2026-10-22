
package com.uade.e_commerce.controller.user;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.uade.e_commerce.dto.user.UserRequestDTO;
import com.uade.e_commerce.dto.user.UserResponseDTO;
import com.uade.e_commerce.exception.EmailAlreadyExistsException;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.Role;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.security.JwtService;
import com.uade.e_commerce.security.SecurityConfig;
import com.uade.e_commerce.service.UserService;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private UserResponseDTO buildUserResponse() {
        User account = new User(
                1L,
                "Ada",
                "Lovelace",
                "ada@test.com",
                "hash",
                "L1",
                LocalDateTime.now(),
                true,
                Role.USER
        );

        return UserResponseDTO.fromEntity(account);
    }

    private RequestPostProcessor authenticatedUser(Long id, Role role) {

        // The real JWT filter uses our User entity as the principal.
        // Tests must reproduce that principal, including its database ID.
        User principal = new User();
        principal.setId(id);
        principal.setEmail("user" + id + "@test.com");
        principal.setPassword("hash");
        principal.setEnabled(true);
        principal.setRole(role);

        return user(principal);
    }

    @Test
    void getAllUsers_admin_returnsList() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(List.of(buildUserResponse()));

        mockMvc.perform(get("/api/users")
                        .with(authenticatedUser(5L, Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("ada@test.com"));
    }

    @Test
    void getAllUsers_withoutAdminRole_returns403() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(authenticatedUser(2L, Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById_owner_returnsOk() throws Exception {
        when(userService.getUserById(1L))
                .thenReturn(buildUserResponse());

        mockMvc.perform(get("/api/users/1")
                        .with(authenticatedUser(1L, Role.USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getUserById_otherUser_returns403() throws Exception {
        mockMvc.perform(get("/api/users/1")
                        .with(authenticatedUser(2L, Role.USER)))
                .andExpect(status().isForbidden());

        verify(userService, never()).getUserById(anyLong());
    }

    @Test
    void getUserById_adminCanAccessOtherUser() throws Exception {
        when(userService.getUserById(1L))
                .thenReturn(buildUserResponse());

        mockMvc.perform(get("/api/users/1")
                        .with(authenticatedUser(5L, Role.ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_notFound_returns404() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/api/users/99")
                        .with(authenticatedUser(99L, Role.USER)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_owner_returnsOk() throws Exception {
        when(userService.updateUser(
                eq(1L),
                any(UserRequestDTO.class)))
                .thenReturn(buildUserResponse());

        mockMvc.perform(put("/api/users/1")
                        .with(authenticatedUser(1L, Role.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Ada",
                                    "lastName": "Lovelace",
                                    "email": "ada@test.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ada@test.com"));
    }

    @Test
    void updateUser_otherUser_returns403WithoutUpdating() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .with(authenticatedUser(2L, Role.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Intruso",
                                    "email": "intruso@test.com",
                                    "password": "newPassword"
                                }
                                """))
                .andExpect(status().isForbidden());

        verify(userService, never()).updateUser(
                anyLong(),
                any(UserRequestDTO.class)
        );
    }

    @Test
    void updateUser_adminCanUpdateOtherUser() throws Exception {
        when(userService.updateUser(
                eq(1L),
                any(UserRequestDTO.class)))
                .thenReturn(buildUserResponse());

        mockMvc.perform(put("/api/users/1")
                        .with(authenticatedUser(5L, Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Ada",
                                    "email": "ada@test.com"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_notFound_returns404() throws Exception {
        when(userService.updateUser(
                eq(99L),
                any(UserRequestDTO.class)))
                .thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(put("/api/users/99")
                        .with(authenticatedUser(99L, Role.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Ada",
                                    "email": "ada@test.com"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_duplicateEmail_returns409() throws Exception {
        when(userService.updateUser(
                eq(1L),
                any(UserRequestDTO.class)))
                .thenThrow(
                        new EmailAlreadyExistsException("taken@test.com")
                );

        mockMvc.perform(put("/api/users/1")
                        .with(authenticatedUser(1L, Role.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "firstName": "Ada",
                                    "email": "taken@test.com"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteUser_owner_returnsNoContent() throws Exception {
        when(userService.deleteUser(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/1")
                        .with(authenticatedUser(1L, Role.USER)))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_otherUser_returns403WithoutDeleting() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(authenticatedUser(2L, Role.USER)))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    void deleteUser_adminCanDeleteOtherUser() throws Exception {
        when(userService.deleteUser(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/1")
                        .with(authenticatedUser(5L, Role.ADMIN)))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_notFound_returns404() throws Exception {
        when(userService.deleteUser(99L))
                .thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(delete("/api/users/99")
                        .with(authenticatedUser(99L, Role.USER)))
                .andExpect(status().isNotFound());
    }
}
