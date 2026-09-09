package com.uade.e_commerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.cart.CartItemResponseDTO;
import com.uade.e_commerce.dto.cart.CartResponseDTO;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.Cart;
import com.uade.e_commerce.model.CartItem;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.CartItemRepository;
import com.uade.e_commerce.repository.CartRepository;
import com.uade.e_commerce.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public CartService(
        CartRepository cartRepository,
        CartItemRepository cartItemRepository,
        UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
    }

    public CartResponseDTO getCart(Long userId) {

        User user = getUser(userId);

        Cart cart = cartRepository
            .findByUserId(userId)
            .orElseGet(() -> createCart(user));

        return buildResponse(cart);
    }

    private User getUser(Long userId) {

        return userRepository
            .findById(userId)
            .orElseThrow(() ->
                new UserNotFoundException(userId)
            );
    }

    private Cart createCart(User user) {

        Cart cart = new Cart();

        cart.setUser(user);

        return cartRepository.save(cart);
    }

    private CartResponseDTO buildResponse(Cart cart) {

        List<CartItem> cartItems =
            cartItemRepository.findByCartIdOrderByIdAsc(
                cart.getId()
            );

        List<CartItemResponseDTO> items =
            cartItems
                .stream()
                .map(CartItemResponseDTO::fromEntity)
                .collect(Collectors.toList());

        double total =
            items
                .stream()
                .mapToDouble(CartItemResponseDTO::getSubtotal)
                .sum();

        return new CartResponseDTO(
            cart.getId(),
            cart.getUser().getId(),
            items,
            total
        );
    }
}
