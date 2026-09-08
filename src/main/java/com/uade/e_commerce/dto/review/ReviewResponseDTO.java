package com.uade.e_commerce.dto.review;

import java.time.LocalDateTime;

import com.uade.e_commerce.model.Review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {

    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime date;
    private Long productId;
    private Long userId;

    public static ReviewResponseDTO fromEntity(Review review) {
        return new ReviewResponseDTO(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getDate(),
                review.getProduct().getId(),
                review.getUser().getId());
    }
}
