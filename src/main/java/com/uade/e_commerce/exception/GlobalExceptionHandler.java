package com.uade.e_commerce.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Centralizes translating exceptions into HTTP responses, so controllers
// don't get cluttered with try/catch and every error comes out in the same
// format. To add a new exception it's enough to add another
// @ExceptionHandler method and reuse buildResponse(), which already builds
// the JSON.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(
        EmailAlreadyExistsException ex
    ) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(
        InvalidCredentialsException ex
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // =========================
    // 404 - NOT FOUND
    // =========================

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFound(
        ProductNotFoundException ex
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryNotFound(
        CategoryNotFoundException ex
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(
        UserNotFoundException ex
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOrderNotFound(
        OrderNotFoundException ex
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReviewNotFound(
        ReviewNotFoundException ex
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ProductImageNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductImageNotFound(
        ProductImageNotFoundException ex
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // =========================
    // 400 - BAD REQUEST
    // =========================

    @ExceptionHandler(NegativePriceException.class)
    public ResponseEntity<Map<String, Object>> handleNegativePrice(
        NegativePriceException ex
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidQuantity(
        InvalidQuantityException ex
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOrderState(
        InvalidOrderStateException ex
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 400 and not 409: an empty cart isn't a conflict against another
    // operation, it's a request that can't be fulfilled as it was sent.
    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<Map<String, Object>> handleEmptyCart(
        EmptyCartException ex
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidArgument(
        IllegalArgumentException ex
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // =========================
    // 409 - CONFLICT
    // =========================

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(
        InsufficientStockException ex
    ) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DuplicateReviewException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateReview(
        DuplicateReviewException ex
    ) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // =========================
    // 500 - INTERNAL SERVER ERROR
    // =========================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralErrors(Exception ex) {
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Error interno del servidor"
        );
    }

    // A Map is used instead of a DTO so Part 7 can define the final error
    // format without having to undo one of our classes. It's a LinkedHashMap
    // and not a HashMap so the keys always come out in the same order in the
    // JSON.
    private ResponseEntity<Map<String, Object>> buildResponse(
        HttpStatus status,
        String message
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
