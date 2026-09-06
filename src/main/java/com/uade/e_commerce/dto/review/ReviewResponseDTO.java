package com.uade.e_commerce.dto.review;

import java.time.LocalDateTime;

import com.uade.e_commerce.model.Review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lo que devolvemos al frontend cuando pide reseñas.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {

    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private Long productId;
    private Long userId;

    // Mandamos también el nombre del autor (además del id) para que el
    // frontend pueda mostrar "Juan Pérez - 5 estrellas" sin tener que
    // pedir cada usuario por separado. Mismo criterio que usa
    // ProductResponseDTO con la categoría.
    private String userFullName;

    public static ReviewResponseDTO fromEntity(Review review) {
        return new ReviewResponseDTO(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getProduct().getId(),
                review.getUser().getId(),
                review.getUser().getFirstName() + " " + review.getUser().getLastName());
    }
}
