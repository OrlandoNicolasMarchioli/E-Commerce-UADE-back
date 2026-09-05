package com.uade.e_commerce.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long id) {
        super("No se encontró la categoría con id: " + id);
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }
}
