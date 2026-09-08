package com.uade.e_commerce.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("No se encontró el producto con id: " + id);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
