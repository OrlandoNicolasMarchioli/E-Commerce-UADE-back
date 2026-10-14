
package com.uade.e_commerce.controller.product;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.uade.e_commerce.dto.category.CategoryResponseDTO;
import com.uade.e_commerce.dto.product.ProductRequestDTO;
import com.uade.e_commerce.dto.product.ProductResponseDTO;
import com.uade.e_commerce.exception.CategoryNotFoundException;
import com.uade.e_commerce.exception.NegativePriceException;
import com.uade.e_commerce.exception.ProductNotFoundException;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.ProductType;
import com.uade.e_commerce.security.JwtService;
import com.uade.e_commerce.service.ProductService;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

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

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_validData_returnsCreated() throws Exception {
        when(productService.createProduct(
                any(ProductRequestDTO.class),
                eq(2L)
        )).thenReturn(buildProductResponse());

        mockMvc.perform(post("/api/products?publisherId=2")
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
                .andExpect(jsonPath("$.name").value("Notebook"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_categoryNotFound_returns404() throws Exception {
        when(productService.createProduct(
                any(ProductRequestDTO.class),
                eq(2L)
        )).thenThrow(new CategoryNotFoundException(99L));

        mockMvc.perform(post("/api/products?publisherId=2")
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
    @WithMockUser(roles = "ADMIN")
    void createProduct_publisherNotFound_returns404() throws Exception {
        when(productService.createProduct(
                any(ProductRequestDTO.class),
                eq(99L)
        )).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(post("/api/products?publisherId=99")
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
    @WithMockUser(roles = "ADMIN")
    void createProduct_negativePrice_returns400() throws Exception {
        when(productService.createProduct(
                any(ProductRequestDTO.class),
                eq(2L)
        )).thenThrow(new NegativePriceException());

        mockMvc.perform(post("/api/products?publisherId=2")
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

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_validData_returnsOk() throws Exception {
        when(productService.updateProduct(
                eq(1L),
                any(ProductRequestDTO.class)
        )).thenReturn(buildProductResponse());

        mockMvc.perform(put("/api/products/1")
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
    @WithMockUser(roles = "ADMIN")
    void updateProduct_categoryNotFound_returns404() throws Exception {
        when(productService.updateProduct(
                eq(1L),
                any(ProductRequestDTO.class)
        )).thenThrow(new CategoryNotFoundException(99L));

        mockMvc.perform(put("/api/products/1")
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
    @WithMockUser(roles = "ADMIN")
    void updateProduct_productNotFound_returns404() throws Exception {
        when(productService.updateProduct(
                eq(99L),
                any(ProductRequestDTO.class)
        )).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(put("/api/products/99")
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
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_found_returnsNoContent() throws Exception {
        when(productService.deleteProduct(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_notFound_returns404() throws Exception {
        when(productService.deleteProduct(99L))
                .thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound());
    }
}
