package com.uade.e_commerce.controller.category;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.category.CategoryRequestDTO;
import com.uade.e_commerce.dto.category.CategoryResponseDTO;
import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.service.CategoryService;

// http://localhost:8080/api/category
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping()
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryService.getAllCategories().stream()
                .map(CategoryResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(CategoryResponseDTO.fromEntity(category));
    }

    @PostMapping()
    public CategoryResponseDTO createCategory(@RequestBody CategoryRequestDTO dto) {
        Category created = categoryService.createCategory(dto.toEntity());
        return CategoryResponseDTO.fromEntity(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryRequestDTO dto) {
        Category updated = categoryService.updateCategory(id, dto.toEntity());
        return ResponseEntity.ok(CategoryResponseDTO.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
