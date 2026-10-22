
package com.uade.e_commerce.controller.product;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.uade.e_commerce.dto.category.CategoryResponseDTO;
import com.uade.e_commerce.dto.product.ProductRequestDTO;
import com.uade.e_commerce.dto.product.ProductResponseDTO;
import com.uade.e_commerce.exception.CategoryNotFoundException;
import com.uade.e_commerce.exception.NegativePriceException;
import com.uade.e_commerce.exception.ProductNotFoundException;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.ProductType;
import com.uade.e_commerce.model.Role;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.security.JwtService;
import com.uade.e_commerce.security.SecurityConfig;
import com.uade.e_commerce.service.ProductService;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // Simula un administrador autenticado con un ID real.
    private RequestPostProcessor admin(Long id) {
        User principal = new User();

        principal.setId(id);
        principal.setEmail("admin" + id + "@test.com");
        principal.setPassword("hash");
        principal.setRole(Role.ADMIN);
        principal.setEnabled(true);

        return user(principal);
    }

    private ProductResponseDTO buildProductResponse() {
        return new ProductResponseDTO(
                1L,
                "Notebook",
                "desc",
                new BigDecimal("100.00"),
                ProductType.PHYSICAL,
                5,
                null,
                null,
                new CategoryResponseDTO(1L, "Tecnología"),
                2L
        );
    }

    // -----------------------------
    // CONSULTAS PUBLICAS
    // -----------------------------

    @Test
    void getAllProducts_returnsList() throws Exception {
        when(productService.getAllProducts())
                .thenReturn(List.of(buildProductResponse()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Notebook"));
    }

    @Test
    void getProductsByCategory_returnsList() throws Exception {
        when(productService.getProductsByCategory(1L))
                .thenReturn(List.of(buildProductResponse()));

        mockMvc.perform(get("/api/products/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category.id").value(1));
    }

    @Test
    void getProductById_found_returnsOk() throws Exception {
        when(productService.getProductById(1L))
                .thenReturn(buildProductResponse());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.publisherId").value(2));
    }

    @Test
    void getProductById_notFound_returns404() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    // -----------------------------
    // CREACION DE PRODUCTOS
    // -----------------------------

    @Test
    void createProduct_validData_returnsCreated() throws Exception {
        when(productService.createProduct(
                any(ProductRequestDTO.class),
                eq(2L)
        )).thenReturn(buildProductResponse());

        mockMvc.perform(post("/api/products")
                        .with(admin(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Notebook",
                                    "price": 100.00,
                                    "type": "PHYSICAL",
                                    "categoryId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.publisherId").value(2));

        verify(productService).createProduct(
                any(ProductRequestDTO.class),
                eq(2L)
        );
    }

    @Test
    void createProduct_categoryNotFound_returns404() throws Exception {
        when(productService.createProduct(
                any(ProductRequestDTO.class),
                eq(2L)
        )).thenThrow(new CategoryNotFoundException(99L));

        mockMvc.perform(post("/api/products")
                        .with(admin(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Notebook",
                                    "price": 100.00,
                                    "type": "PHYSICAL",
                                    "categoryId": 99
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_publisherNotFound_returns404() throws Exception {
        when(productService.createProduct(
                any(ProductRequestDTO.class),
                eq(99L)
        )).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(post("/api/products")
                        .with(admin(99L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Notebook",
                                    "price": 100.00,
                                    "type": "PHYSICAL",
                                    "categoryId": 1
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_negativePrice_returns400() throws Exception {
        when(productService.createProduct(
                any(ProductRequestDTO.class),
                eq(2L)
        )).thenThrow(new NegativePriceException());

        mockMvc.perform(post("/api/products")
                        .with(admin(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Notebook",
                                    "price": -10.00,
                                    "type": "PHYSICAL",
                                    "categoryId": 1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // Verifica que un precio nulo retorne 400 y no 500.
    @Test
    void createProduct_missingPrice_returns400() throws Exception {
        when(productService.createProduct(
                any(ProductRequestDTO.class),
                eq(2L)
        )).thenThrow(new IllegalArgumentException(
                "El precio es obligatorio"
        ));

        mockMvc.perform(post("/api/products")
                        .with(admin(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Notebook",
                                    "type": "PHYSICAL",
                                    "categoryId": 1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // Aunque el cliente envie otro publisherId,
    // se utiliza exclusivamente el ID del usuario autenticado.
    @Test
    void createProduct_cannotImpersonatePublisher() throws Exception {
        when(productService.createProduct(
                any(ProductRequestDTO.class),
                eq(2L)
        )).thenReturn(buildProductResponse());

        mockMvc.perform(post("/api/products?publisherId=99")
                        .with(admin(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Notebook",
                                    "price": 100.00,
                                    "type": "PHYSICAL",
                                    "categoryId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publisherId").value(2));

        verify(productService).createProduct(
                any(ProductRequestDTO.class),
                eq(2L)
        );

        verify(productService, never()).createProduct(
                any(ProductRequestDTO.class),
                eq(99L)
        );
    }

    // -----------------------------
    // SEGURIDAD
    // -----------------------------

    @Test
    void createProduct_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Notebook",
                                    "price": 100.00,
                                    "type": "PHYSICAL",
                                    "categoryId": 1
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProduct_withoutAdminRole_returns403() throws Exception {
        mockMvc.perform(post("/api/products")
                        .with(user("student").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Notebook",
                                    "price": 100.00,
                                    "type": "PHYSICAL",
                                    "categoryId": 1
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    // -----------------------------
    // ACTUALIZACION DE PRODUCTOS
    // -----------------------------

    @Test
    void updateProduct_validData_returnsOk() throws Exception {
        when(productService.updateProduct(
                eq(1L),
                any(ProductRequestDTO.class)
        )).thenReturn(buildProductResponse());

        mockMvc.perform(put("/api/products/1")
                        .with(admin(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Notebook",
                                    "price": 100.00,
                                    "type": "PHYSICAL",
                                    "categoryId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook"));
    }

    @Test
    void updateProduct_categoryNotFound_returns404() throws Exception {
        when(productService.updateProduct(
                eq(1L),
                any(ProductRequestDTO.class)
        )).thenThrow(new CategoryNotFoundException(99L));

        mockMvc.perform(put("/api/products/1")
                        .with(admin(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Notebook",
                                    "price": 100.00,
                                    "type": "PHYSICAL",
                                    "categoryId": 99
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_productNotFound_returns404() throws Exception {
        when(productService.updateProduct(
                eq(99L),
                any(ProductRequestDTO.class)
        )).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(put("/api/products/99")
                        .with(admin(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Notebook",
                                    "price": 100.00,
                                    "type": "PHYSICAL",
                                    "categoryId": 1
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    // -----------------------------
    // ELIMINACION DE PRODUCTOS
    // -----------------------------

    @Test
    void deleteProduct_found_returnsNoContent() throws Exception {
        when(productService.deleteProduct(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/api/products/1")
                        .with(admin(2L)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_notFound_returns404() throws Exception {
        when(productService.deleteProduct(99L))
                .thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(delete("/api/products/99")
                        .with(admin(2L)))
                .andExpect(status().isNotFound());
    }
}
