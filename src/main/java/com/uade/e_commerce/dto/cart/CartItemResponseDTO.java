package com.uade.e_commerce.dto.cart;

import com.uade.e_commerce.model.CartItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {

    private Long productId;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;

    public static CartItemResponseDTO fromEntity(CartItem cartItem) {

        Double unitPrice = cartItem.getProduct().getPrice();
        Double subtotal = unitPrice * cartItem.getQuantity();

        return new CartItemResponseDTO(
            cartItem.getProduct().getId(),
            cartItem.getProduct().getName(),
            cartItem.getQuantity(),
            unitPrice,
            subtotal
        );
    }
}