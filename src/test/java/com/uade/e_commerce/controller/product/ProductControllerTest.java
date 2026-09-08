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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.uade.e_commerce.exception.CategoryNotFoundException;
import com.uade.e_commerce.exception.ProductNotFoundException;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductType;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.service.CategoryService;
import com.uade.e_commerce.service.ProductService;
import com.uade.e_commerce.service.UserService;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private UserService userService;

    private Product buildProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Notebook");
        product.setDescription("desc");
        product.setPrice(100.0);
        product.setType(ProductType.PHYSICAL);
        product.setStock(5);
        product.setCategory(new Category(1L, "Tecnología", null));
        product.setPublisher(new User(2L, "Ada", "Lovelace", "ada@test.com", "hash", "L1", null, true));
        return product;
    }

    @Test
    void getAllProducts_returnsList() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(buildProduct()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Notebook"));
    }

    @Test
    void getProductsByCategory_returnsList() throws Exception {
        when(productService.getProductsByCategory(1L)).thenReturn(List.of(buildProduct()));

        mockMvc.perform(get("/api/products/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getProductById_found_returnsOk() throws Exception {
        when(productService.getProductById(1L)).thenReturn(buildProduct());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.publisherId").value(2));
    }

    @Test
    void getProductById_notFound_returns404() throws Exception {
        when(productService.getProductById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_categoryAndPublisherExist_returnsOk() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(new Category(1L, "Tecnología", null));
        when(userService.getUserById(2L)).thenReturn(new User(2L, "Ada", "Lovelace", "ada@test.com", "hash", "L1", null, true));
        when(productService.createProduct(any(Product.class))).thenReturn(buildProduct());

        mockMvc.perform(post("/api/products?publisherId=2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Notebook\",\"price\":100.0,\"type\":\"PHYSICAL\",\"categoryId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notebook"));
    }

    @Test
    void createProduct_categoryDoesNotExist_returns404() throws Exception {
        when(categoryService.getCategoryById(99L)).thenThrow(new CategoryNotFoundException(99L));

        mockMvc.perform(post("/api/products?publisherId=2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Notebook\",\"price\":100.0,\"type\":\"PHYSICAL\",\"categoryId\":99}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_publisherDoesNotExist_returns404() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(new Category(1L, "Tecnología", null));
        when(userService.getUserById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(post("/api/products?publisherId=99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Notebook\",\"price\":100.0,\"type\":\"PHYSICAL\",\"categoryId\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_categoryExists_returnsOk() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(new Category(1L, "Tecnología", null));
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(buildProduct());

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Notebook\",\"price\":100.0,\"type\":\"PHYSICAL\",\"categoryId\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateProduct_categoryDoesNotExist_returns404() throws Exception {
        when(categoryService.getCategoryById(99L)).thenThrow(new CategoryNotFoundException(99L));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Notebook\",\"price\":100.0,\"type\":\"PHYSICAL\",\"categoryId\":99}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_productNotFound_returns404() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(new Category(1L, "Tecnología", null));
        when(productService.updateProduct(eq(99L), any(Product.class))).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(put("/api/products/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Notebook\",\"price\":100.0,\"type\":\"PHYSICAL\",\"categoryId\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_found_returnsNoContent() throws Exception {
        when(productService.deleteProduct(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_notFound_returns404() throws Exception {
        when(productService.deleteProduct(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound());
    }
}
