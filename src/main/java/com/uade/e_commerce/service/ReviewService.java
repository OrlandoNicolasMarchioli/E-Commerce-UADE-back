package com.uade.e_commerce.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.review.ReviewRequestDTO;
import com.uade.e_commerce.exception.DuplicateReviewException;
import com.uade.e_commerce.exception.ProductNotFoundException;
import com.uade.e_commerce.exception.ReviewNotFoundException;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.Review;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.ProductRepository;
import com.uade.e_commerce.repository.ReviewRepository;
import com.uade.e_commerce.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository,
            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public List<Review> getReviewsByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        return reviewRepository.findByProductIdOrderByDateDesc(productId);
    }

    public Review getReviewById(Long productId, Long reviewId) {
        return findOwnedReview(productId, reviewId);
    }

    public Review createReview(Long productId, Long userId, ReviewRequestDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new DuplicateReviewException(userId, productId);
        }

        Review review = new Review();
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setDate(LocalDateTime.now());
        review.setProduct(product);
        review.setUser(user);

        return reviewRepository.save(review);
    }

    public Review updateReview(Long productId, Long reviewId, ReviewRequestDTO dto) {
        Review review = findOwnedReview(productId, reviewId);

        // On purpose, the product and user of an already-created review can't
        // be changed — only its content.
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        return reviewRepository.save(review);
    }

    public void deleteReview(Long productId, Long reviewId) {
        Review review = findOwnedReview(productId, reviewId);
        reviewRepository.delete(review);
    }

    // Looks up the review by id and validates that it belongs to the
    // product in the URL. If it doesn't, it's treated as "not found"
    // instead of a 400/403: within the scope of
    // /products/{productId}/reviews, that review doesn't exist.
    private Review findOwnedReview(Long productId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!review.getProduct().getId().equals(productId)) {
            throw new ReviewNotFoundException(reviewId);
        }

        return review;
    }
}
