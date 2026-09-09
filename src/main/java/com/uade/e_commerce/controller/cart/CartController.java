package com.uade.e_commerce.controller.cart;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.cart.CartItemRequestDTO;
import com.uade.e_commerce.dto.cart.CartResponseDTO;
import com.uade.e_commerce.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // GET http://localhost:8080/api/cart?userId=1
    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(
        @RequestParam Long userId
    ) {

        return ResponseEntity.ok(
            cartService.getCart(userId)
        );
    }

    // POST http://localhost:8080/api/cart/items?userId=1
    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItem(
        @RequestParam Long userId,
        @RequestBody CartItemRequestDTO dto
    ) {

        CartResponseDTO cart =
            cartService.addItem(userId, dto);

        return new ResponseEntity<>(
            cart,
            HttpStatus.CREATED
        );
    }

    // DELETE http://localhost:8080/api/cart/items/3?userId=1
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(
        @RequestParam Long userId,
        @PathVariable Long productId
    ) {

        cartService.removeItem(
            userId,
            productId
        );

        return ResponseEntity
            .noContent()
            .build();
    }

    // DELETE http://localhost:8080/api/cart?userId=1
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
        @RequestParam Long userId
    ) {

        cartService.clearCart(userId);

        return ResponseEntity
            .noContent()
            .build();
    }
}