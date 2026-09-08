package com.uade.e_commerce.controller.category;

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

import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.service.CategoryService;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void getAllCategories_returnsList() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(new Category(1L, "Cursos", "desc")));

        mockMvc.perform(get("/api/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Cursos"));
    }

    @Test
    void getCategoryById_found_returnsOk() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(new Category(1L, "Cursos", "desc"));

        mockMvc.perform(get("/api/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cursos"));
    }

    @Test
    void getCategoryById_notFound_returns404() throws Exception {
        when(categoryService.getCategoryById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/category/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCategory_returnsCreatedCategory() throws Exception {
        when(categoryService.createCategory(any(Category.class))).thenReturn(new Category(1L, "Cursos", null));

        mockMvc.perform(post("/api/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cursos\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateCategory_found_returnsOk() throws Exception {
        when(categoryService.updateCategory(eq(1L), any(Category.class)))
                .thenReturn(new Category(1L, "Nuevo nombre", null));

        mockMvc.perform(put("/api/category/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nuevo nombre\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nuevo nombre"));
    }

    @Test
    void updateCategory_notFound_returns404() throws Exception {
        when(categoryService.updateCategory(eq(99L), any(Category.class))).thenReturn(null);

        mockMvc.perform(put("/api/category/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_found_returnsNoContent() throws Exception {
        when(categoryService.deleteCategory(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/category/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategory_notFound_returns404() throws Exception {
        when(categoryService.deleteCategory(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/category/99"))
                .andExpect(status().isNotFound());
    }
}
