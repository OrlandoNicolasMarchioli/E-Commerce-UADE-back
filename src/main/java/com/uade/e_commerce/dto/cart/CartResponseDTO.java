package com.uade.e_commerce.dto.cart;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Represents the cart returned to the frontend. The total is calculated
// from the current item prices instead of being persisted in the database.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {

    private Long id;
    private Long userId;
    private List<CartItemResponseDTO> items;
    private Double total;
}