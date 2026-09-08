package com.uade.e_commerce.service;

import java.util.List;

import org.springframework.stereotype.Service;

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
        return productRepository.findById(id).orElse(null);
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product product) {
        Product existing = productRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
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
            return false;
        }
        productRepository.deleteById(id);
        return true;
    }

}
