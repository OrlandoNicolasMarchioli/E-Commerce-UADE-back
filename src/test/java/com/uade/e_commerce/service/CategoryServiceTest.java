
package com.uade.e_commerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.e_commerce.dto.category.CategoryRequestDTO;
import com.uade.e_commerce.dto.category.CategoryResponseDTO;
import com.uade.e_commerce.exception.CategoryNotFoundException;
import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getAllCategories_returnsDTOList() {
        Category category = new Category(1L, "Cursos", "desc");

        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Cursos");
    }

    @Test
    void getCategoryById_found_returnsCategory() {
        Category category = new Category(1L, "Cursos", "desc");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        Category result = categoryService.getCategoryById(1L);

        assertThat(result).isEqualTo(category);
    }

    @Test
    void getCategoryDetails_found_returnsDTO() {
        Category category = new Category(1L, "Cursos", "desc");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        CategoryResponseDTO result = categoryService.getCategoryDetails(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Cursos");
    }

    @Test
    void getCategoryById_notFound_throwsCategoryNotFoundException() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void createCategory_savesAndReturnsDTO() {
        CategoryRequestDTO request = new CategoryRequestDTO("Cursos");
        Category saved = new Category(1L, "Cursos", null);

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponseDTO result = categoryService.createCategory(request);

        ArgumentCaptor<Category> captor =
                ArgumentCaptor.forClass(Category.class);

        verify(categoryRepository).save(captor.capture());

        assertThat(captor.getValue().getName()).isEqualTo("Cursos");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Cursos");
    }

    @Test
    void updateCategory_found_updatesNameAndReturnsDTO() {
        Category existing = new Category(1L, "Viejo nombre", "desc");
        CategoryRequestDTO request = new CategoryRequestDTO("Nuevo nombre");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponseDTO result = categoryService.updateCategory(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Nuevo nombre");
        assertThat(existing.getDescription()).isEqualTo("desc");
    }

    @Test
    void updateCategory_notFound_throwsAndDoesNotSave() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        CategoryRequestDTO request = new CategoryRequestDTO("Nuevo nombre");

        assertThatThrownBy(() -> categoryService.updateCategory(99L, request))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteCategory_found_deletesAndReturnsTrue() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        boolean result = categoryService.deleteCategory(1L);

        assertThat(result).isTrue();
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_notFound_throwsAndDoesNotDelete() {
        when(categoryRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository, never()).deleteById(any());
    }
}
