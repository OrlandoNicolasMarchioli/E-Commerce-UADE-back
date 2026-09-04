package com.uade.e_commerce.dto.product;

import lombok.Data;

@Data
public class ProductImageResponseDTO {
    private Long id;
    private String url;
    private Integer imageOrder;
    private Long productId;
}
