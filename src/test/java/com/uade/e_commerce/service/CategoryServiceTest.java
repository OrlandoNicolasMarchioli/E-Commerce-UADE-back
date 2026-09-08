package com.uade.e_commerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getAllCategories_returnsWhatTheRepositoryReturns() {
        Category category = new Category(1L, "Cursos", "desc");
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<Category> result = categoryService.getAllCategories();

        assertThat(result).containsExactly(category);
    }

    @Test
    void getCategoryById_found_returnsCategory() {
        Category category = new Category(1L, "Cursos", "desc");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryById(1L);

        assertThat(result).isEqualTo(category);
    }

    @Test
    void getCategoryById_notFound_returnsNull() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        Category result = categoryService.getCategoryById(99L);

        assertThat(result).isNull();
    }

    @Test
    void createCategory_savesAndReturnsPersisted() {
        Category toSave = new Category(null, "Cursos", "desc");
        Category saved = new Category(1L, "Cursos", "desc");
        when(categoryRepository.save(toSave)).thenReturn(saved);

        Category result = categoryService.createCategory(toSave);

        assertThat(result).isEqualTo(saved);
    }

    @Test
    void updateCategory_found_updatesNameAndSaves() {
        Category existing = new Category(1L, "Viejo nombre", "desc");
        Category changes = new Category(null, "Nuevo nombre", null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.updateCategory(1L, changes);

        assertThat(result.getName()).isEqualTo("Nuevo nombre");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void updateCategory_notFound_returnsNull() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        Category result = categoryService.updateCategory(99L, new Category(null, "x", null));

        assertThat(result).isNull();
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_found_deletesAndReturnsTrue() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        boolean result = categoryService.deleteCategory(1L);

        assertThat(result).isTrue();
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCategory_notFound_returnsFalseWithoutDeleting() {
        when(categoryRepository.existsById(anyLong())).thenReturn(false);

        boolean result = categoryService.deleteCategory(99L);

        assertThat(result).isFalse();
        verify(categoryRepository, never()).deleteById(any());
    }
}
