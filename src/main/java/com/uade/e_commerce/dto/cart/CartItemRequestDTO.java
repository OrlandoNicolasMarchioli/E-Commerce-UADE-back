package com.uade.e_commerce.dto.cart;

import lombok.Data;

// What comes in the body when adding an item to the cart.
// The frontend sends the product id instead of a full Product object,
// keeping the DTO independent from JPA entities.
@Data
public class CartItemRequestDTO {

    private Long productId;
    private Integer quantity;
}