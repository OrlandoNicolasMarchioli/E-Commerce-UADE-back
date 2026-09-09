package com.uade.e_commerce.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException(Long userId) {
        super(
            "No se puede crear un pedido con el carrito vacío. Usuario: " +
                userId
        );
    }

    public EmptyCartException(String message) {
        super(message);
    }
}
