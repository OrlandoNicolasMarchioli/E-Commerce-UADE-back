package com.uade.e_commerce.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce.model.Category;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void save_thenFindById_returnsPersistedCategory() {
        Category category = categoryRepository.save(new Category(null, "Cursos", "Cursos y clases"));

        Category found = categoryRepository.findById(category.getId()).orElseThrow();

        assertThat(found.getName()).isEqualTo("Cursos");
        assertThat(found.getDescription()).isEqualTo("Cursos y clases");
    }

    @Test
    void existsById_afterSave_isTrue() {
        Category category = categoryRepository.save(new Category(null, "Útiles", null));

        assertThat(categoryRepository.existsById(category.getId())).isTrue();
    }

    @Test
    void deleteById_removesCategory() {
        Category category = categoryRepository.save(new Category(null, "Temporal", null));
        Long id = category.getId();

        categoryRepository.deleteById(id);

        assertThat(categoryRepository.existsById(id)).isFalse();
    }
}
