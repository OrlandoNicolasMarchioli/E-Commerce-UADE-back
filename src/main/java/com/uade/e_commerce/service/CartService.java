package com.uade.e_commerce.service;

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

    // =========================
    // GET CART
    // =========================

    public CartResponseDTO getCart(Long userId) {

        User user = getUser(userId);

        Cart cart = cartRepository
            .findByUserId(userId)
            .orElseGet(() -> createCart(user));

        return buildResponse(cart);
    }

    // =========================
    // ADD ITEM
    // =========================

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

        // Physical products manage stock.
        // Services don't use stock.
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

    // =========================
    // REMOVE ITEM
    // =========================

    public void removeItem(
        Long userId,
        Long productId
    ) {

        getUser(userId);

        Cart cart = cartRepository
            .findByUserId(userId)
            .orElse(null);

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

    // =========================
    // CLEAR CART
    // =========================

    public void clearCart(Long userId) {

        getUser(userId);

        Cart cart = cartRepository
            .findByUserId(userId)
            .orElse(null);

        if (cart == null) {
            return;
        }

        cartItemRepository.deleteByCartId(
            cart.getId()
        );
    }

    // =========================
    // HELPERS
    // =========================

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

        double total =
            items
                .stream()
                .mapToDouble(
                    CartItemResponseDTO::getSubtotal
                )
                .sum();

        return new CartResponseDTO(
            cart.getId(),
            cart.getUser().getId(),
            items,
            total
        );
    }
}