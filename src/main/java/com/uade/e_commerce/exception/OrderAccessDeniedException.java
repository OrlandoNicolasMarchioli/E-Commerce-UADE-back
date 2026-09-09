package com.uade.e_commerce.exception;

public class OrderAccessDeniedException extends RuntimeException {

    public OrderAccessDeniedException(
        Long orderId,
        Long userId
    ) {
        super(
            "El pedido " +
                orderId +
                " no pertenece al usuario " +
                userId
        );
    }

    public OrderAccessDeniedException(String message) {
        super(message);
    }
}
