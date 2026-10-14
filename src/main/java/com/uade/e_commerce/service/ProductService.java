
package com.uade.e_commerce.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.product.ProductRequestDTO;
import com.uade.e_commerce.dto.product.ProductResponseDTO;
import com.uade.e_commerce.exception.CategoryNotFoundException;
import com.uade.e_commerce.exception.NegativePriceException;
import com.uade.e_commerce.exception.ProductNotFoundException;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.CategoryRepository;
import com.uade.e_commerce.repository.ProductRepository;
import com.uade.e_commerce.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAllByOrderByNameAsc()
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .toList();
    }

    public List<ProductResponseDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(ProductResponseDTO::fromEntity)
                .toList();
    }

    public ProductResponseDTO getProductById(Long id) {
        return ProductResponseDTO.fromEntity(findProductById(id));
    }

    public ProductResponseDTO createProduct(
            ProductRequestDTO dto,
            Long publisherId) {

        validatePrice(dto.getPrice());

        // Relationships are resolved here so the controller never
        // needs to retrieve or manipulate JPA entities.
        Category category = findCategoryById(dto.getCategoryId());
        User publisher = findUserById(publisherId);

        Product product = dto.toEntity();
        product.setCategory(category);
        product.setPublisher(publisher);

        Product saved = productRepository.save(product);

        return ProductResponseDTO.fromEntity(saved);
    }

    public ProductResponseDTO updateProduct(
            Long id,
            ProductRequestDTO dto) {

        Product existing = findProductById(id);

        validatePrice(dto.getPrice());

        Category category = findCategoryById(dto.getCategoryId());

        // Updating the existing entity preserves its identity
        // and the relationships that cannot be modified by this request.
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setType(dto.getType());
        existing.setStock(dto.getStock());
        existing.setMinutesDuration(dto.getMinutesDuration());
        existing.setAttendanceType(dto.getAttendanceType());
        existing.setCategory(category);

        // The original publisher must remain unchanged.
        Product updated = productRepository.save(existing);

        return ProductResponseDTO.fromEntity(updated);
    }

    public boolean deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(
                    "Producto no encontrado con id: " + id
            );
        }

        productRepository.deleteById(id);
        return true;
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Producto no encontrado con id: " + id
                        )
                );
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(id)
                );
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(id)
                );
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException(
                    "El precio es obligatorio"
            );
        }

        // compareTo ignores BigDecimal scale, unlike equals.
        // Values such as 0 and 0.00 are treated consistently.
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativePriceException();
        }
    }
}
