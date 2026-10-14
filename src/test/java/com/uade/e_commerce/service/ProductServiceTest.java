
package com.uade.e_commerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.e_commerce.dto.product.ProductRequestDTO;
import com.uade.e_commerce.dto.product.ProductResponseDTO;
import com.uade.e_commerce.exception.CategoryNotFoundException;
import com.uade.e_commerce.exception.NegativePriceException;
import com.uade.e_commerce.exception.ProductNotFoundException;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductType;
import com.uade.e_commerce.model.Role;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.CategoryRepository;
import com.uade.e_commerce.repository.ProductRepository;
import com.uade.e_commerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    private Category buildCategory() {
        return new Category(1L, "Tecnología", null);
    }

    private User buildPublisher() {
        return new User(
                2L,
                "Ada",
                "Lovelace",
                "ada@test.com",
                "hash",
                "L1",
                null,
                true,
                Role.USER
        );
    }

    private Product buildProduct(Long id, String name) {
        Product product = new Product();

        product.setId(id);
        product.setName(name);
        product.setDescription("desc");
        product.setPrice(new BigDecimal("100.00"));
        product.setType(ProductType.PHYSICAL);
        product.setStock(10);
        product.setCategory(buildCategory());
        product.setPublisher(buildPublisher());

        return product;
    }

    private ProductRequestDTO buildRequest(Long categoryId) {
        return new ProductRequestDTO(
                "Notebook",
                "desc",
                new BigDecimal("100.00"),
                ProductType.PHYSICAL,
                10,
                null,
                null,
                categoryId
        );
    }

    @Test
    void getAllProducts_returnsOrderedDTOs() {
        when(productRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(buildProduct(1L, "Notebook")));

        List<ProductResponseDTO> result =
                productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Notebook");
        assertThat(result.get(0).getPublisherId()).isEqualTo(2L);
    }

    @Test
    void getProductsByCategory_returnsDTOs() {
        when(productRepository.findByCategoryId(1L))
                .thenReturn(List.of(buildProduct(1L, "Notebook")));

        List<ProductResponseDTO> result =
                productService.getProductsByCategory(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory().getId()).isEqualTo(1L);
    }

    @Test
    void getProductById_found_returnsDTO() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(buildProduct(1L, "Notebook")));

        ProductResponseDTO result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Notebook");
    }

    @Test
    void getProductById_notFound_throwsProductNotFoundException() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void createProduct_validData_savesAndReturnsDTO() {
        ProductRequestDTO request = buildRequest(1L);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(buildCategory()));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(buildPublisher()));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        ProductResponseDTO result =
                productService.createProduct(request, 2L);

        ArgumentCaptor<Product> captor =
                ArgumentCaptor.forClass(Product.class);

        verify(productRepository).save(captor.capture());

        assertThat(captor.getValue().getCategory().getId())
                .isEqualTo(1L);

        assertThat(captor.getValue().getPublisher().getId())
                .isEqualTo(2L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Notebook");
        assertThat(result.getPublisherId()).isEqualTo(2L);
    }

    @Test
    void createProduct_categoryNotFound_throwsException() {
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.createProduct(buildRequest(99L), 2L)
        ).isInstanceOf(CategoryNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_publisherNotFound_throwsException() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(buildCategory()));

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.createProduct(buildRequest(1L), 99L)
        ).isInstanceOf(UserNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_negativePrice_throwsException() {
        ProductRequestDTO request = buildRequest(1L);
        request.setPrice(new BigDecimal("-10.00"));

        assertThatThrownBy(() ->
                productService.createProduct(request, 2L)
        ).isInstanceOf(NegativePriceException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_missingPrice_throwsException() {
        ProductRequestDTO request = buildRequest(1L);
        request.setPrice(null);

        assertThatThrownBy(() ->
                productService.createProduct(request, 2L)
        ).isInstanceOf(IllegalArgumentException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_found_updatesFieldsAndKeepsPublisher() {
        Product existing = buildProduct(1L, "Nombre viejo");
        User originalPublisher = existing.getPublisher();

        ProductRequestDTO request = new ProductRequestDTO(
                "Nombre nuevo",
                "desc nueva",
                new BigDecimal("200.00"),
                ProductType.SERVICE,
                5,
                30,
                "Remoto",
                2L
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(categoryRepository.findById(2L))
                .thenReturn(Optional.of(
                        new Category(2L, "Servicios", null)
                ));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponseDTO result =
                productService.updateProduct(1L, request);

        assertThat(result.getName()).isEqualTo("Nombre nuevo");
        assertThat(result.getPrice())
                .isEqualByComparingTo("200.00");
        assertThat(result.getType()).isEqualTo(ProductType.SERVICE);
        assertThat(result.getCategory().getId()).isEqualTo(2L);
        assertThat(result.getPublisherId()).isEqualTo(2L);
        assertThat(existing.getPublisher()).isSameAs(originalPublisher);
    }

    @Test
    void updateProduct_notFound_throwsException() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.updateProduct(99L, buildRequest(1L))
        ).isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_categoryNotFound_throwsException() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(buildProduct(1L, "Notebook")));

        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.updateProduct(1L, buildRequest(99L))
        ).isInstanceOf(CategoryNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_negativePrice_throwsException() {
        ProductRequestDTO request = buildRequest(1L);
        request.setPrice(new BigDecimal("-1.00"));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(buildProduct(1L, "Notebook")));

        assertThatThrownBy(() ->
                productService.updateProduct(1L, request)
        ).isInstanceOf(NegativePriceException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_found_deletesAndReturnsTrue() {
        when(productRepository.existsById(1L))
                .thenReturn(true);

        boolean result = productService.deleteProduct(1L);

        assertThat(result).isTrue();
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_notFound_throwsException() {
        when(productRepository.existsById(anyLong()))
                .thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).deleteById(anyLong());
    }
}
