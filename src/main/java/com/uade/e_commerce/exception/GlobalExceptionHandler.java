package com.uade.e_commerce.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Centraliza la traducción de excepciones a respuestas HTTP, así los controllers
// no se llenan de try/catch y todos los errores salen con el mismo formato.
//
// NOTA PARA LA PARTE 7 (manejo global de errores):
// acá solo se manejan a propósito las dos excepciones de esta entrega, para no
// pisar ese trabajo. Para sumar una nueva alcanza con agregar otro método
// @ExceptionHandler y reutilizar buildResponse(), que ya deja el JSON armado.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // Se usa un Map y no un DTO para que la Parte 7 pueda definir el formato
    // definitivo del error sin tener que deshacer una clase nuestra. Es
    // LinkedHashMap y no HashMap para que las claves salgan siempre en el mismo
    // orden en el JSON.
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
