package com.uade.e_commerce.exception;

public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String state, String operation) {
        super(
            "No se puede realizar la operación '" +
                operation +
                "' cuando la orden se encuentra en estado '" +
                state +
                "'"
        );
    }

    public InvalidOrderStateException(String message) {
        super(message);
    }
}
