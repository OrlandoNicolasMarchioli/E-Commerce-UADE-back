package com.uade.e_commerce.service;

import com.uade.e_commerce.dto.product.ProductImageRequestDTO;
import com.uade.e_commerce.dto.product.ProductImageResponseDTO;
import com.uade.e_commerce.exception.ProductImageNotFoundException;
import com.uade.e_commerce.exception.ProductNotFoundException;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductImage;
import com.uade.e_commerce.repository.ProductImageRepository;
import com.uade.e_commerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductImageService {
    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public ProductImageResponseDTO addImageToProduct(
        Long productId,
        ProductImageRequestDTO requestDTO
    ) {
        Product product = productRepository
            .findById(productId)
            .orElseThrow(() ->
                new ProductNotFoundException(
                    "Producto no encontrado con id: " + productId
                )
            );

        ProductImage productImage = new ProductImage();
        productImage.setUrl(requestDTO.getUrl());
        productImage.setImageOrder(requestDTO.getImageOrder());
        productImage.setProduct(product);

        ProductImage savedProductImage = productImageRepository.save(productImage);

        return mapToDTO(savedProductImage);
    }

    @Transactional
    public void deleteProductImage(Long productId, Long imageId) {
        ProductImage productImage = productImageRepository
            .findById(imageId)
            .orElseThrow(() ->
                new ProductImageNotFoundException(
                    "Imagen de producto no encontrada con id: " + imageId
                )
            );

        if (!productImage.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException(
                "La imagen de producto no pertenece al producto especificado"
            );
        }

        productImageRepository.delete(productImage);
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponseDTO> getImagesByProductId(Long productId) {
        productRepository
            .findById(productId)
            .orElseThrow(() ->
                new ProductNotFoundException(
                    "Producto no encontrado con id: " + productId
                )
            );

        return productImageRepository.findByProductIdOrderByImageOrderAsc(productId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ProductImageResponseDTO mapToDTO(ProductImage productImage) {
        ProductImageResponseDTO responseDTO = new ProductImageResponseDTO();
        responseDTO.setId(productImage.getId());
        responseDTO.setUrl(productImage.getUrl());
        responseDTO.setImageOrder(productImage.getImageOrder());
        responseDTO.setProductId(productImage.getProduct().getId());
        return responseDTO;
    }
}