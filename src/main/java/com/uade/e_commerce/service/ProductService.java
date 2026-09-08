package com.uade.e_commerce.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.uade.e_commerce.exception.NegativePriceException;
import com.uade.e_commerce.exception.ProductNotFoundException;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        // select * from products
        return productRepository.findAllByOrderByNameAsc();
    }

    public List<Product> getProductsByCategory(Long categoryId) {
    return productRepository.findByCategoryId(categoryId);

    }

    public Product getProductById(Long id) {
        return productRepository
            .findById(id)
            .orElseThrow(() ->
                new ProductNotFoundException(
                    "Producto no encontrado con id: " + id
                )
            );
    }

    public Product createProduct(Product product) {
        if (product.getPrice() < 0) {
            throw new NegativePriceException();
        }

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product product) {
        Product existing = productRepository
            .findById(id)
            .orElseThrow(() ->
                new ProductNotFoundException(
                    "Producto no encontrado con id: " + id
                )
            );

        if (product.getPrice() < 0) {
            throw new NegativePriceException();
        }

        // We update field by field (instead of replacing the whole entity)
        // so we don't lose the id or the original publisher.
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setType(product.getType());
        existing.setStock(product.getStock());
        existing.setMinutesDuration(product.getMinutesDuration());
        existing.setAttendanceType(product.getAttendanceType());
        existing.setCategory(product.getCategory());

        // on purpose we do NOT touch "publisher" here. A product's owner
        // shouldn't be able to change with a simple update.
        return productRepository.save(existing);
    
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

}
