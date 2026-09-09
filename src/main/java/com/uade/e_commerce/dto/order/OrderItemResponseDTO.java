package com.uade.e_commerce.dto.order;

import com.uade.e_commerce.model.OrderItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {

    private Long productId;
    private String productName;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;

    // The unit price comes from the OrderItem and not from the Product,
    // because it's the one that was charged. The subtotal is the only value
    // calculated here: being quantity * unitPrice, storing it would only
    // create a column that can end up out of sync with the other two.
    public static OrderItemResponseDTO fromEntity(OrderItem orderItem) {

        Double subtotal =
            orderItem.getUnitPrice() * orderItem.getQuantity();

        return new OrderItemResponseDTO(
            orderItem.getProduct().getId(),
            orderItem.getProductName(),
            orderItem.getQuantity(),
            orderItem.getUnitPrice(),
            subtotal
        );
    }
}
