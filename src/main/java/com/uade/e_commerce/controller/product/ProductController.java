package com.uade.e_commerce.controller.product;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.product.ProductRequestDTO;
import com.uade.e_commerce.dto.product.ProductResponseDTO;
import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.service.CategoryService;
import com.uade.e_commerce.service.ProductService;
import com.uade.e_commerce.service.UserService;



// http://localhost:8080/api/products
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;

    // We need CategoryService and UserService here (and not just
    // ProductService) because when creating/editing a product we have to
    // validate that the category and the publisher actually exist in the
    // database before saving it.

    ProductController(ProductService productService, CategoryService categoryService, UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
    }


    // get http://localhost:8080/api/products (alphabetical order for the home page)
    @GetMapping()
    public List<ProductResponseDTO> getAllProducts() {
        return productService.getAllProducts().stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // get http://localhost:8080/api/products/category/1
    // Filter by category, for the home page's "product types" section.
    @GetMapping("/category/{categoryId}")
    public List<ProductResponseDTO> getProductsByCategory(@PathVariable Long categoryId) {
        return productService.getProductsByCategory(categoryId).stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // get http://localhost:8080/api/products/1
    // Detail of a specific product.
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ProductResponseDTO.fromEntity(product));

    }

    // post http://localhost:8080/api/products?publisherId=1

    // publisherId goes as a query param (instead of being pulled from a
    // logged-in session) because there's still no login in the project.
    // Once the login part is ready, this should be replaced with the
    // authenticated user's id.


    @PostMapping()
    public ResponseEntity<ProductResponseDTO> createProduct(
            @RequestBody ProductRequestDTO dto,
            @RequestParam Long publisherId) {

        // We validate that the category and the user exist BEFORE creating
        // the product, so we don't end up with an "orphan" product or an
        // ugly referential-integrity error in the database.

        Category category = categoryService.getCategoryById(dto.getCategoryId());
        User publisher = userService.getUserById(publisherId);
        if (category == null || publisher == null) {
            return ResponseEntity.badRequest().build();
        }

        Product product = dto.toEntity();
        product.setCategory(category);
        product.setPublisher(publisher);

        Product created = productService.createProduct(product);
        return ResponseEntity.ok(ProductResponseDTO.fromEntity(created));
    }

    // put http://localhost:8080/api/products/1
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id, @RequestBody ProductRequestDTO dto) {
        Category category = categoryService.getCategoryById(dto.getCategoryId());
        if (category == null) {
            return ResponseEntity.badRequest().build();
        }

        Product product = dto.toEntity();
        product.setCategory(category);

        Product updated = productService.updateProduct(id, product);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ProductResponseDTO.fromEntity(updated));
    }

    // delete http://localhost:8080/api/products/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
