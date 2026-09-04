package com.uade.e_commerce.service;

import com.uade.e_commerce.dto.product.ProductImageRequestDTO;
import com.uade.e_commerce.dto.product.ProductImageResponseDTO;
import com.uade.e_commerce.model.ProductImage;
import com.uade.e_commerce.model.Product;
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
    public ProductImageResponseDTO addImageToProduct(Long productId, ProductImageRequestDTO requestDTO) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        ProductImage productImage = new ProductImage();
        productImage.setUrl(requestDTO.getUrl());
        productImage.setImageOrder(requestDTO.getImageOrder());
        productImage.setProduct(product);

        ProductImage savedProductImage = productImageRepository.save(productImage);

        return mapToDTO(savedProductImage);
    }

    @Transactional
    public void deleteProductImage(Long productId, Long imageId) {
        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Product image not found with id: " + imageId));

        if (!productImage.getProduct().getId().equals(productId)) {
            throw new RuntimeException("Product image does not belong to the specified product");
        }

        productImageRepository.delete(productImage);
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponseDTO> getImagesByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

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