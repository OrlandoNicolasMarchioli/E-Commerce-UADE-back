package com.uade.e_commerce.dto.cart;

import java.math.BigDecimal;

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
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    // Price and subtotal are calculated from the current Product price.
    // They aren't stored in CartItem because the cart doesn't freeze prices;
    // that should happen later when an order is created.
    public static CartItemResponseDTO fromEntity(CartItem cartItem) {

        BigDecimal unitPrice =
            cartItem.getProduct().getPrice();

        BigDecimal subtotal =
            unitPrice.multiply(
                BigDecimal.valueOf(cartItem.getQuantity())
            );

        return new CartItemResponseDTO(
            cartItem.getProduct().getId(),
            cartItem.getProduct().getName(),
            cartItem.getQuantity(),
            unitPrice,
            subtotal
        );
    }
}