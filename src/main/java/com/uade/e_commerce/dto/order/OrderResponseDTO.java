package com.uade.e_commerce.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// The order as the frontend sees it. The status travels as text (the enum
// name) so the API doesn't expose the internal type and the response stays
// readable.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private Long userId;
    private LocalDateTime orderDate;
    private String status;
    private List<OrderItemResponseDTO> items;
    private BigDecimal total;
}
