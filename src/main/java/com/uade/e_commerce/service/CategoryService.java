
package com.uade.e_commerce.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.category.CategoryRequestDTO;
import com.uade.e_commerce.dto.category.CategoryResponseDTO;
import com.uade.e_commerce.exception.CategoryNotFoundException;
import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.repository.CategoryRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponseDTO::fromEntity)
                .toList();
    }

    // ProductController still needs the entity until product
    // creation and updating are moved into ProductService.
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Categoría no encontrada con id: " + id
                        )
                );
    }

    public CategoryResponseDTO getCategoryDetails(Long id) {
        return CategoryResponseDTO.fromEntity(getCategoryById(id));
    }

    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {
        Category category = dto.toEntity();
        Category saved = categoryRepository.save(category);

        return CategoryResponseDTO.fromEntity(saved);
    }

    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto) {
        Category existing = getCategoryById(id);

        // Updating the managed entity preserves its identity
        // and any fields that are not editable through this DTO.
        existing.setName(dto.getName());

        Category updated = categoryRepository.save(existing);

        return CategoryResponseDTO.fromEntity(updated);
    }

    public boolean deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException(
                    "Categoría no encontrada con id: " + id
            );
        }

        categoryRepository.deleteById(id);
        return true;
    }
}
