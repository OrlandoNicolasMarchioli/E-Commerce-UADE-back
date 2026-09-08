package com.uade.e_commerce.exception;

public class ProductImageNotFoundException extends RuntimeException {

    public ProductImageNotFoundException(Long id) {
        super("No se encontró la imagen del producto con id: " + id);
    }

    public ProductImageNotFoundException(String message) {
        super(message);
    }
}
