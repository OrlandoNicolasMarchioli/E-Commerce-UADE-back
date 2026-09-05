package com.uade.e_commerce.exception;

public class InvalidQuantityException extends RuntimeException {

    public InvalidQuantityException() {
        super("La cantidad debe ser mayor a 0");
    }

    public InvalidQuantityException(Integer quantity) {
        super("La cantidad debe ser mayor a 0. Valor recibido: " + quantity);
    }

    public InvalidQuantityException(String message) {
        super(message);
    }
}
