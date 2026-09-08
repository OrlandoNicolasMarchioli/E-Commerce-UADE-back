package com.uade.e_commerce.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("No se encontró la orden con id: " + id);
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}
