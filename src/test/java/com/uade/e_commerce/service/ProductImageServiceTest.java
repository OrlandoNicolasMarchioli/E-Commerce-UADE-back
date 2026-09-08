package com.uade.e_commerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.e_commerce.dto.product.ProductImageRequestDTO;
import com.uade.e_commerce.dto.product.ProductImageResponseDTO;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductImage;
import com.uade.e_commerce.repository.ProductImageRepository;
import com.uade.e_commerce.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductImageService productImageService;

    @Test
    void addImageToProduct_productExists_savesAndMapsToDto() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductImageRequestDTO request = new ProductImageRequestDTO();
        request.setUrl("http://img/1.png");
        request.setImageOrder(1);

        ProductImage saved = new ProductImage();
        saved.setId(10L);
        saved.setUrl("http://img/1.png");
        saved.setImageOrder(1);
        saved.setProduct(product);
        when(productImageRepository.save(any(ProductImage.class))).thenReturn(saved);

        ProductImageResponseDTO result = productImageService.addImageToProduct(1L, request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUrl()).isEqualTo("http://img/1.png");
        assertThat(result.getProductId()).isEqualTo(1L);
    }

    @Test
    void addImageToProduct_productNotFound_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productImageService.addImageToProduct(99L, new ProductImageRequestDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");

        verify(productImageRepository, never()).save(any());
    }

    @Test
    void deleteProductImage_belongsToProduct_deletes() {
        Product product = new Product();
        product.setId(1L);
        ProductImage image = new ProductImage();
        image.setId(5L);
        image.setProduct(product);
        when(productImageRepository.findById(5L)).thenReturn(Optional.of(image));

        productImageService.deleteProductImage(1L, 5L);

        verify(productImageRepository).delete(image);
    }

    @Test
    void deleteProductImage_imageNotFound_throws() {
        when(productImageRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productImageService.deleteProductImage(1L, 5L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void deleteProductImage_belongsToDifferentProduct_throwsAndDoesNotDelete() {
        Product otherProduct = new Product();
        otherProduct.setId(2L);
        ProductImage image = new ProductImage();
        image.setId(5L);
        image.setProduct(otherProduct);
        when(productImageRepository.findById(5L)).thenReturn(Optional.of(image));

        assertThatThrownBy(() -> productImageService.deleteProductImage(1L, 5L))
                .isInstanceOf(RuntimeException.class);

        verify(productImageRepository, never()).delete(any());
    }

    @Test
    void getImagesByProductId_productExists_returnsOrderedList() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductImage image = new ProductImage();
        image.setId(1L);
        image.setUrl("http://img/1.png");
        image.setImageOrder(1);
        image.setProduct(product);
        when(productImageRepository.findByProductIdOrderByImageOrderAsc(1L)).thenReturn(List.of(image));

        List<ProductImageResponseDTO> result = productImageService.getImagesByProductId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(1L);
    }

    @Test
    void getImagesByProductId_productNotFound_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productImageService.getImagesByProductId(99L))
                .isInstanceOf(RuntimeException.class);
    }
}
