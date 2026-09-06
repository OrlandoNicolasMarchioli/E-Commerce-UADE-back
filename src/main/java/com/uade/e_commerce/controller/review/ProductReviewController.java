package com.uade.e_commerce.controller.review;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.uade.e_commerce.dto.review.ReviewRequestDTO;
import com.uade.e_commerce.dto.review.ReviewResponseDTO;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.service.ProductService;
import com.uade.e_commerce.service.ReviewService;
import com.uade.e_commerce.service.UserService;

/**
 * CRUD de reseñas de un producto.
 *
 * La ruta arranca con /api/products/{productId} porque una reseña no existe
 * sola: siempre es "la reseña DE este producto". Es el mismo criterio que ya
 * usa ProductImageController con las imágenes.
 */

// http://localhost:8080/api/products/1/reviews
@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ProductReviewController {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final ReviewService reviewService;
    private final ProductService productService;
    private final UserService userService;

    // Necesitamos ProductService y UserService además de ReviewService porque
    // antes de guardar una reseña hay que verificar que el producto y el autor
    // existan de verdad en la base.
    ProductReviewController(ReviewService reviewService, ProductService productService, UserService userService) {
        this.reviewService = reviewService;
        this.productService = productService;
        this.userService = userService;
    }

    // get http://localhost:8080/api/products/1/reviews
    @GetMapping()
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByProduct(@PathVariable Long productId) {
        if (productService.getProductById(productId) == null) {
            return ResponseEntity.notFound().build();
        }
        // Ojo: un producto sin reseñas devuelve 200 con una lista vacía, no 404.
        // "No hay reseñas" es una respuesta válida; 404 significaría que el
        // producto no existe, que es otra cosa.
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    // get http://localhost:8080/api/products/1/reviews/5
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable Long productId, @PathVariable Long reviewId) {
        ReviewResponseDTO review = reviewService.getReview(productId, reviewId);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(review);
    }

    // post http://localhost:8080/api/products/1/reviews?userId=1
    //
    // userId va como query param (y no sacado de una sesión logueada) por la
    // misma razón que publisherId en ProductController: todavía no hay login.
    // Cuando esté, esto se reemplaza por el id del usuario autenticado.
    @PostMapping()
    public ResponseEntity<ReviewResponseDTO> createReview(
            @PathVariable Long productId,
            @RequestParam Long userId,
            @RequestBody ReviewRequestDTO dto) {

        Product product = productService.getProductById(productId);
        if (product == null) {
            // 404: el recurso de la URL no existe.
            return ResponseEntity.notFound().build();
        }

        User user = userService.getUserById(userId);
        if (user == null || !isValidRating(dto.getRating())) {
            // 400: la URL está bien, pero lo que mandó el cliente no sirve.
            return ResponseEntity.badRequest().build();
        }

        if (reviewService.hasUserReviewedProduct(productId, userId)) {
            // 409 Conflict: no es un error de formato, es que ya existe.
            // El usuario debería editar su reseña en vez de crear otra.
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        ReviewResponseDTO created = reviewService.createReview(product, user, dto);
        // 201 Created es el status correcto para un POST que crea algo nuevo.
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // put http://localhost:8080/api/products/1/reviews/5
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @RequestBody ReviewRequestDTO dto) {

        if (!isValidRating(dto.getRating())) {
            return ResponseEntity.badRequest().build();
        }

        ReviewResponseDTO updated = reviewService.updateReview(productId, reviewId, dto);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    // delete http://localhost:8080/api/products/1/reviews/5
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long productId, @PathVariable Long reviewId) {
        if (!reviewService.deleteReview(productId, reviewId)) {
            return ResponseEntity.notFound().build();
        }
        // 204 No Content: se borró bien y no hay nada que devolver.
        return ResponseEntity.noContent().build();
    }

    /**
     * La calificación tiene que ser un número del 1 al 5.
     *
     * Lo validamos a mano porque el proyecto todavía no usa
     * spring-boot-starter-validation (las anotaciones tipo @Min/@Max).
     * Si más adelante se agrega esa dependencia, este método se reemplaza
     * por un @Valid en el parámetro y anotaciones en el DTO.
     */
    private boolean isValidRating(Integer rating) {
        return rating != null && rating >= MIN_RATING && rating <= MAX_RATING;
    }
}
