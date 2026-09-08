package com.uade.e_commerce.exception;

// It's a RuntimeException (unchecked) so we don't have to force the
// controller or the intermediate layers to declare throws: the service
// throws it and the GlobalExceptionHandler takes care of translating it
// into a 409.
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Ya existe un usuario registrado con el email: " + email);
    }
}
