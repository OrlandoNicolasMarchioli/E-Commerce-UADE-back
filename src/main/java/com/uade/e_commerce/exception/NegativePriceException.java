package com.uade.e_commerce.exception;

public class NegativePriceException extends RuntimeException {

    public NegativePriceException() {
        super("El precio debe ser mayor a 0");
    }

    public NegativePriceException(String message) {
        super(message);
    }
}
