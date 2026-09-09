package com.uade.e_commerce.service;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import com.uade.e_commerce.exception.ProductNotFoundException;
import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductType;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product buildProduct(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("desc");
        product.setPrice(new BigDecimal("100.00"));
        product.setType(ProductType.PHYSICAL);
        product.setStock(10);
        Category category = new Category(1L, "Cursos", null);
        User publisher = new User(1L, "Ada", "Lovelace", "ada@test.com", "hash", "L1", null, true);
        product.setCategory(category);
        product.setPublisher(publisher);
        return product;
    }

    @Test
    void getAllProducts_delegatesToOrderedFinder() {
        Product product = buildProduct(1L, "Notebook");
        when(productRepository.findAllByOrderByNameAsc()).thenReturn(List.of(product));

        List<Product> result = productService.getAllProducts();

        assertThat(result).containsExactly(product);
    }

    @Test
    void getProductsByCategory_delegatesToRepository() {
        Product product = buildProduct(1L, "Notebook");
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(product));

        List<Product> result = productService.getProductsByCategory(1L);

        assertThat(result).containsExactly(product);
    }

    @Test
    void getProductById_found_returnsProduct() {
        Product product = buildProduct(1L, "Notebook");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThat(productService.getProductById(1L)).isEqualTo(product);
    }

    @Test
    void getProductById_notFound_throwsProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void createProduct_savesAndReturnsPersisted() {
        Product toSave = buildProduct(null, "Notebook");
        Product saved = buildProduct(1L, "Notebook");
        when(productRepository.save(toSave)).thenReturn(saved);

        Product result = productService.createProduct(toSave);

        assertThat(result).isEqualTo(saved);
    }

    @Test
    void updateProduct_found_updatesFieldsButKeepsOriginalPublisher() {
        Product existing = buildProduct(1L, "Nombre viejo");
        User originalPublisher = existing.getPublisher();

        Product changes = new Product();
        changes.setName("Nombre nuevo");
        changes.setDescription("desc nueva");
        changes.setPrice(new BigDecimal("200.00"));
        changes.setType(ProductType.SERVICE);
        changes.setStock(5);
        changes.setMinutesDuration(30);
        changes.setAttendanceType("Remoto");
        changes.setCategory(new Category(2L, "Servicios", null));
        // On purpose we don't set publisher on "changes": the service
        // shouldn't overwrite the original publisher on an update.

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct(1L, changes);

        assertThat(result.getName()).isEqualTo("Nombre nuevo");
        assertThat(result.getPrice()).isEqualByComparingTo("200.00");
        assertThat(result.getType()).isEqualTo(ProductType.SERVICE);
        assertThat(result.getCategory().getId()).isEqualTo(2L);
        assertThat(result.getPublisher()).isEqualTo(originalPublisher);
    }

    @Test
    void updateProduct_notFound_throwsAndDoesNotSave() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, buildProduct(null, "x")))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_found_deletesAndReturnsTrue() {
        when(productRepository.existsById(1L)).thenReturn(true);

        assertThat(productService.deleteProduct(1L)).isTrue();
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_notFound_throwsAndDoesNotDelete() {
        when(productRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }
}
