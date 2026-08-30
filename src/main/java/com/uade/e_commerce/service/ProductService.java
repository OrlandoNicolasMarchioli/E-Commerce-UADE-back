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
        // select * from productos
        return productRepository.findAllByOrderByNombreAsc();
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

        // Actualizamos campo por campo (en vez de reemplazar la entidad
        // entera) para no perder el id ni el publicador original.
        existing.setNombre(product.getNombre());
        existing.setDescripcion(product.getDescripcion());
        existing.setPrecio(product.getPrecio());
        existing.setType(product.getType());
        existing.setStock(product.getStock());
        existing.setDuracionMinutos(product.getDuracionMinutos());
        existing.setModalidad(product.getModalidad());
        existing.setCategory(product.getCategory());

        // a propósito NO tocamos "publicador" acá. El dueño de un
        // producto no debería poder cambiar con un simple update.
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
