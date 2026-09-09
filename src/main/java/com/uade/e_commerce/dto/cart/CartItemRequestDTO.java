package com.uade.e_commerce.dto.cart;

import lombok.Data;

@Data
public class CartItemRequestDTO {

    private Long productId;
    private Integer quantity;
}