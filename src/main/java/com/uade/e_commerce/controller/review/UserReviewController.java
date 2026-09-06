package com.uade.e_commerce.controller.review;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.review.ReviewResponseDTO;
import com.uade.e_commerce.service.ReviewService;
import com.uade.e_commerce.service.UserService;

/**
 * Las reseñas vistas "del lado del usuario": todas las que escribió una persona.
 *
 * Va en un controller aparte porque una clase solo puede tener un
 * RequestMapping de base, y esta cuelga de /api/users en vez de /api/products.
 * Solo tiene GET: crear, editar y borrar se hacen desde ProductReviewController,
 * que es donde la reseña vive.
 */

// http://localhost:8080/api/users/1/reviews
@RestController
@RequestMapping("/api/users/{userId}/reviews")
public class UserReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    UserReviewController(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    // get http://localhost:8080/api/users/1/reviews
    @GetMapping()
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByUser(@PathVariable Long userId) {
        if (userService.getUserById(userId) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }
}
