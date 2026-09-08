package com.uade.e_commerce.dto.review;

import lombok.Data;

@Data
public class ReviewRequestDTO {
    private Integer rating;
    private String comment;
}
