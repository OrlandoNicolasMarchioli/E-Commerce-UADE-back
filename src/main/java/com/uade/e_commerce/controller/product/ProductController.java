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



// http://localhost:8080/api/productos
@RestController
@RequestMapping("/api/productos")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;

    // Necesitamos CategoryService y UserService acá (y no solo
    // ProductService) porque al crear/editar un producto hay que
    // validar que la categoría y el publicador realmente existan
    // en la base antes de guardarlo.

    ProductController(ProductService productService, CategoryService categoryService, UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
    }


    // get http://localhost:8080/api/productos (orden alfabetico para la home)
    @GetMapping()
    public List<ProductResponseDTO> getAllProducts() {
        return productService.getAllProducts().stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // get http://localhost:8080/api/productos/categoria/1
    // Filtro por categoría, para la sección de "tipos de producto" de la home.
    @GetMapping("/categoria/{categoryId}")
    public List<ProductResponseDTO> getProductsByCategory(@PathVariable Long categoryId) {
        return productService.getProductsByCategory(categoryId).stream()
                .map(ProductResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // get http://localhost:8080/api/productos/1
    // Detalle de un producto puntual.
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ProductResponseDTO.fromEntity(product));

    }

    // post http://localhost:8080/api/productos?publicadorId=1

    // publicadorId va como query param (en vez de sacarlo de una sesión
    // logueada) porque todavía no existe el login en el proyecto.
    // Cuando la parte del login esté lista, esto debería reemplazarse
    // por el id del usuario autenticado.


    @PostMapping()
    public ResponseEntity<ProductResponseDTO> createProduct(
            @RequestBody ProductRequestDTO dto,
            @RequestParam Long publicadorId) {

        // Validamos que la categoría y el usuario existan ANTES de crear
        // el producto, para no terminar con un producto "huérfano" o
        // con un error feo de integridad referencial en la base.

        Category category = categoryService.getCategoryById(dto.getCategoryId());
        User publicador = userService.getUserById(publicadorId);
        if (category == null || publicador == null) {
            return ResponseEntity.badRequest().build();
        }

        Product product = dto.toEntity();
        product.setCategory(category);
        product.setPublicador(publicador);

        Product created = productService.createProduct(product);
        return ResponseEntity.ok(ProductResponseDTO.fromEntity(created));
    }

    // put http://localhost:8080/api/productos/1
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

    // delete http://localhost:8080/api/productos/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
