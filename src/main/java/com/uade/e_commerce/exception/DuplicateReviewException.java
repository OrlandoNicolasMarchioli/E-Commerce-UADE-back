package com.uade.e_commerce.exception;

// Thrown when a user tries to review the same product twice (see the
// unique constraint on the reviews table over product_id + user_id).
public class DuplicateReviewException extends RuntimeException {

    public DuplicateReviewException(Long userId, Long productId) {
        super("El usuario " + userId + " ya realizó una reseña para el producto " + productId);
    }
}
