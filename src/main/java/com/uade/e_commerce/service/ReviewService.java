package com.uade.e_commerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.review.ReviewRequestDTO;
import com.uade.e_commerce.dto.review.ReviewResponseDTO;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.Review;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.ReviewRepository;

import jakarta.transaction.Transactional;

/**
 * Lógica de las reseñas.
 *
 * Devuelve DTOs (y no entidades) a propósito: como Review tiene el producto
 * y el usuario en modo LAZY, la conversión a DTO tiene que pasar mientras la
 * transacción sigue abierta, o sea acá adentro. Si la hiciéramos en el
 * controller, Hibernate ya habría cerrado la sesión y explotaría al intentar
 * leer review.getUser().getFirstName().
 */

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<ReviewResponseDTO> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDTO> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public ReviewResponseDTO getReview(Long productId, Long reviewId) {
        Review review = findInProduct(productId, reviewId);
        if (review == null) {
            return null;
        }
        return ReviewResponseDTO.fromEntity(review);
    }

    public boolean hasUserReviewedProduct(Long productId, Long userId) {
        return reviewRepository.existsByProductIdAndUserId(productId, userId);
    }

    // Recibe el Product y el User ya resueltos por el controller, igual que
    // hace ProductController con la categoría y el publicador.
    public ReviewResponseDTO createReview(Product product, User user, ReviewRequestDTO dto) {
        Review review = new Review();
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setProduct(product);
        review.setUser(user);
        // createdAt no se setea: ya viene inicializado en la entidad.

        return ReviewResponseDTO.fromEntity(reviewRepository.save(review));
    }

    public ReviewResponseDTO updateReview(Long productId, Long reviewId, ReviewRequestDTO dto) {
        Review review = findInProduct(productId, reviewId);
        if (review == null) {
            return null;
        }

        // Solo se editan los dos campos que el usuario escribió. El autor,
        // el producto y la fecha de creación quedan intactos: editar una
        // reseña no la convierte en la reseña de otra persona.
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        return ReviewResponseDTO.fromEntity(reviewRepository.save(review));
    }

    public boolean deleteReview(Long productId, Long reviewId) {
        Review review = findInProduct(productId, reviewId);
        if (review == null) {
            return false;
        }
        reviewRepository.delete(review);
        return true;
    }

    /**
     * Busca una reseña y verifica que pertenezca al producto de la URL.
     *
     * Sin este chequeo, un pedido a /products/7/reviews/3 borraría la reseña 3
     * aunque sea de otro producto. Devuelve null si no existe o no corresponde,
     * y en los dos casos el controller responde 404.
     */
    private Review findInProduct(Long productId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || !review.getProduct().getId().equals(productId)) {
            return null;
        }
        return review;
    }
}
