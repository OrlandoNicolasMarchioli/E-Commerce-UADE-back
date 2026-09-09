package com.uade.e_commerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.cart.CartItemRequestDTO;
import com.uade.e_commerce.dto.cart.CartItemResponseDTO;
import com.uade.e_commerce.dto.cart.CartResponseDTO;
import com.uade.e_commerce.exception.InsufficientStockException;
import com.uade.e_commerce.exception.InvalidQuantityException;
import com.uade.e_commerce.exception.ProductNotFoundException;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.Cart;
import com.uade.e_commerce.model.CartItem;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductType;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.CartItemRepository;
import com.uade.e_commerce.repository.CartRepository;
import com.uade.e_commerce.repository.ProductRepository;
import com.uade.e_commerce.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
        CartRepository cartRepository,
        CartItemRepository cartItemRepository,
        ProductRepository productRepository,
        UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // The cart is created lazily the first time the user requests it.
    // This avoids creating empty carts for users who never use the feature.
    public CartResponseDTO getCart(Long userId) {

        User user = getUser(userId);

        Cart cart = cartRepository
            .findByUserId(userId)
            .orElseGet(() -> createCart(user));

        return buildResponse(cart);
    }

    public CartResponseDTO addItem(
        Long userId,
        CartItemRequestDTO dto
    ) {

        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new InvalidQuantityException(dto.getQuantity());
        }

        User user = getUser(userId);

        Product product = productRepository
            .findById(dto.getProductId())
            .orElseThrow(() ->
                new ProductNotFoundException(
                    "Producto no encontrado con id: " +
                    dto.getProductId()
                )
            );

        Cart cart = cartRepository
            .findByUserId(userId)
            .orElseGet(() -> createCart(user));

        // There can only be one row per product and cart. If the product is
        // already present, the requested quantity is added to the existing one.
        CartItem existingItem = cartItemRepository
            .findByCartIdAndProductId(
                cart.getId(),
                product.getId()
            )
            .orElse(null);

        int currentQuantity =
            existingItem == null
                ? 0
                : existingItem.getQuantity();

        int requestedTotal =
            currentQuantity + dto.getQuantity();

        // Services don't manage stock. For physical products we validate the
        // final quantity in the cart, not only the amount being added.
        if (product.getType() == ProductType.PHYSICAL) {

            int availableStock =
                product.getStock() == null
                    ? 0
                    : product.getStock();

            if (requestedTotal > availableStock) {

                throw new InsufficientStockException(
                    product.getId(),
                    requestedTotal,
                    availableStock
                );
            }
        }

        // Adding an item only validates availability; it doesn't reserve or
        // decrease stock. Stock should be updated when the order is created.
        if (existingItem == null) {

            CartItem cartItem = new CartItem();

            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(dto.getQuantity());

            cartItemRepository.save(cartItem);

        } else {

            existingItem.setQuantity(requestedTotal);

            cartItemRepository.save(existingItem);
        }

        return buildResponse(cart);
    }

    public void removeItem(
        Long userId,
        Long productId
    ) {

        getUser(userId);

        Cart cart = cartRepository
            .findByUserId(userId)
            .orElse(null);

        // Removing from a cart that doesn't exist is treated as an idempotent
        // operation: there is simply nothing to remove.
        if (cart == null) {
            return;
        }

        cartItemRepository
            .findByCartIdAndProductId(
                cart.getId(),
                productId
            )
            .ifPresent(cartItemRepository::delete);
    }

    public void clearCart(Long userId) {

        getUser(userId);

        Cart cart = cartRepository
            .findByUserId(userId)
            .orElse(null);

        // Clearing a cart that doesn't exist already satisfies the requested
        // final state, so no error is returned.
        if (cart == null) {
            return;
        }

        cartItemRepository.deleteByCartId(
            cart.getId()
        );
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
            cartItemRepository
                .findByCartIdOrderByIdAsc(
                    cart.getId()
                );

        List<CartItemResponseDTO> items =
            cartItems
                .stream()
                .map(CartItemResponseDTO::fromEntity)
                .collect(Collectors.toList());

        // The total is calculated at response time using current product prices,
        // so the cart always reflects the latest catalog value.
        //
        // With BigDecimal the sum goes through reduce(): there's no
        // mapToDouble equivalent, and going through double would bring back
        // exactly the rounding errors this type avoids.
        BigDecimal total =
            items
                .stream()
                .map(CartItemResponseDTO::getSubtotal)
                .reduce(
                    BigDecimal.ZERO,
                    BigDecimal::add
                );

        return new CartResponseDTO(
            cart.getId(),
            cart.getUser().getId(),
            items,
            total
        );
    }
}