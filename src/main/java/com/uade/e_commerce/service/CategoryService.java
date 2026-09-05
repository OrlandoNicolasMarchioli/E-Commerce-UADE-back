package com.uade.e_commerce.service;

import java.util.List;

import org.springframework.stereotype.Service;

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

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository
            .findById(id)
            .orElseThrow(() ->
                new CategoryNotFoundException(
                    "Categoría no encontrada con id: " + id
                )
            );
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category category) {
        Category existing = categoryRepository
            .findById(id)
            .orElseThrow(() ->
                new CategoryNotFoundException(
                    "Categoría no encontrada con id: " + id
                )
            );

        existing.setName(category.getName());
        return categoryRepository.save(existing);
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