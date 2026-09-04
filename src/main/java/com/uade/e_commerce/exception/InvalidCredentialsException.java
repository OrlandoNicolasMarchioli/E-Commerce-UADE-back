package com.uade.e_commerce.exception;

// Se lanza cuando el login falla, sin importar el motivo.
public class InvalidCredentialsException extends RuntimeException {

    // El mensaje es una constante y el constructor no recibe parámetros a
    // propósito: así es imposible que alguien, sin querer, arme un mensaje
    // distinto para cada caso de error. Si el login respondiera "el email no
    // existe" vs "la password es incorrecta", cualquiera podría averiguar qué
    // emails están registrados probando logins uno por uno.
    public static final String MESSAGE = "Credenciales inválidas";

    public InvalidCredentialsException() {
        super(MESSAGE);
    }
}
