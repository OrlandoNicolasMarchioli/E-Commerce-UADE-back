package com.uade.e_commerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // =========================
    // 404 - NOT FOUND
    // =========================

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> manejarProductoNoEncontrado(
        ProductNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ex.getMessage()
        );
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<String> manejarCategoriaNoEncontrada(
        CategoryNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ex.getMessage()
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> manejarUsuarioNoEncontrado(
        UserNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ex.getMessage()
        );
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<String> manejarOrdenNoEncontrada(
        OrderNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ex.getMessage()
        );
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<String> manejarReviewNoEncontrada(
        ReviewNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ex.getMessage()
        );
    }

    @ExceptionHandler(ProductImageNotFoundException.class)
    public ResponseEntity<String> manejarImagenNoEncontrada(
        ProductImageNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ex.getMessage()
        );
    }

    // =========================
    // 400 - BAD REQUEST
    // =========================

    @ExceptionHandler(NegativePriceException.class)
    public ResponseEntity<String> manejarPrecioNegativo(
        NegativePriceException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<String> manejarCantidadInvalida(
        InvalidQuantityException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<String> manejarEstadoOrdenInvalido(
        InvalidOrderStateException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ex.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> manejarArgumentoInvalido(
        IllegalArgumentException ex
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ex.getMessage()
        );
    }

    // =========================
    // 409 - CONFLICT
    // =========================

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<String> manejarStockInsuficiente(
        InsufficientStockException ex
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    //@ExceptionHandler(DuplicateReviewException.class)
    //public ResponseEntity<String> manejarReviewDuplicada(
    //    DuplicateReviewException ex
    //) {
    //    return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    //}

    // =========================
    // 500 - INTERNAL SERVER ERROR
    // =========================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarErroresGenerales(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            "Error interno del servidor"
        );
    }
}
