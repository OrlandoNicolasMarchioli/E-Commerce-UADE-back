package com.uade.e_commerce.exception;

// Es RuntimeException (unchecked) para no obligar al controller ni a las capas
// intermedias a declarar throws: el service la lanza y el GlobalExceptionHandler
// se encarga de traducirla a un 409.
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Ya existe un usuario registrado con el email: " + email);
    }
}
