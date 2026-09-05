package com.uade.e_commerce.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(Long id) {
        super("No se encontró la reseña con id: " + id);
    }

    public ReviewNotFoundException(String message) {
        super(message);
    }
}
