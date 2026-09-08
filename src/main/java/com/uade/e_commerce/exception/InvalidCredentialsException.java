package com.uade.e_commerce.exception;

// Thrown when login fails, regardless of the reason.
public class InvalidCredentialsException extends RuntimeException {

    // The message is a constant and the constructor takes no parameters on
    // purpose: this way nobody can accidentally build a different message
    // for each error case. If login responded "the email doesn't exist" vs
    // "the password is wrong", anyone could figure out which emails are
    // registered by trying logins one by one.
    public static final String MESSAGE = "Credenciales inválidas";

    public InvalidCredentialsException() {
        super(MESSAGE);
    }
}
