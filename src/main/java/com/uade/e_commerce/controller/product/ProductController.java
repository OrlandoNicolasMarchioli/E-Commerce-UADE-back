package com.uade.e_commerce.controller.product;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.product.ProductRequestDTO;
import com.uade.e_commerce.dto.product.ProductResponseDTO;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.service.ProductService;



// http://localhost:8080/api/productos
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }


    // get http://localhost:8080/api/productos
    @GetMapping()
    public List<ProductResponseDTO> getAllProducts() {
        return productService.getAllProducts().stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // get http://localhost:8080/api/productos/1
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ProductResponseDTO.fromEntity(product));
    }

    // post http://localhost:8080/api/productos
    @PostMapping()
    public ResponseEntity<ProductResponseDTO> saveProduct(@RequestBody ProductRequestDTO productRequestDTO) {
        Product product = productService.saveProduct(productRequestDTO.toEntity());
        return ResponseEntity.ok(ProductResponseDTO.fromEntity(product));
    }
}
