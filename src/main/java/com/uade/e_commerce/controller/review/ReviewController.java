package com.uade.e_commerce.controller.review;

import java.util.List;
import java.util.stream.Collectors;

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
import com.uade.e_commerce.model.Review;
import com.uade.e_commerce.service.ReviewService;

// http://localhost:8080/api/products/{productId}/reviews
@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // get http://localhost:8080/api/products/1/reviews
    @GetMapping
    public List<ReviewResponseDTO> getReviewsByProduct(@PathVariable Long productId) {
        return reviewService.getReviewsByProduct(productId).stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // get http://localhost:8080/api/products/1/reviews/1
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> getReviewById(
            @PathVariable Long productId,
            @PathVariable Long reviewId) {
        Review review = reviewService.getReviewById(productId, reviewId);
        return ResponseEntity.ok(ReviewResponseDTO.fromEntity(review));
    }

    // post http://localhost:8080/api/products/1/reviews?userId=1
    //
    // userId goes as a query param, same as publisherId when creating
    // products: there's still no login/session to pull the authenticated
    // user from.
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @PathVariable Long productId,
            @RequestParam Long userId,
            @RequestBody ReviewRequestDTO dto) {
        Review created = reviewService.createReview(productId, userId, dto);
        return new ResponseEntity<>(ReviewResponseDTO.fromEntity(created), HttpStatus.CREATED);
    }

    // put http://localhost:8080/api/products/1/reviews/1
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @RequestBody ReviewRequestDTO dto) {
        Review updated = reviewService.updateReview(productId, reviewId, dto);
        return ResponseEntity.ok(ReviewResponseDTO.fromEntity(updated));
    }

    // delete http://localhost:8080/api/products/1/reviews/1
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId) {
        reviewService.deleteReview(productId, reviewId);
        return ResponseEntity.noContent().build();
    }
}
