package com.uade.e_commerce.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uade.e_commerce.dto.cart.CartItemRequestDTO;
import com.uade.e_commerce.dto.cart.CartResponseDTO;
import com.uade.e_commerce.exception.InsufficientStockException;
import com.uade.e_commerce.exception.InvalidQuantityException;
import com.uade.e_commerce.model.Cart;
import com.uade.e_commerce.model.CartItem;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductType;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.CartItemRepository;
import com.uade.e_commerce.repository.CartRepository;
import com.uade.e_commerce.repository.ProductRepository;
import com.uade.e_commerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        return user;
    }

    private Cart buildCart(User user) {
        Cart cart = new Cart();
        cart.setId(10L);
        cart.setUser(user);
        return cart;
    }

    private Product buildPhysicalProduct(
        Integer stock
    ) {
        Product product = new Product();
        product.setId(20L);
        product.setName("Cuaderno");
        product.setPrice(1000.0);
        product.setType(ProductType.PHYSICAL);
        product.setStock(stock);
        return product;
    }

    private CartItemRequestDTO buildRequest(
        Long productId,
        Integer quantity
    ) {
        CartItemRequestDTO dto =
            new CartItemRequestDTO();

        dto.setProductId(productId);
        dto.setQuantity(quantity);

        return dto;
    }

    @Test
    void getCart_existingCart_returnsCart() {

        User user = buildUser();
        Cart cart = buildCart(user);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdOrderByIdAsc(10L)
        ).thenReturn(List.of());

        CartResponseDTO result =
            cartService.getCart(1L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotal()).isEqualTo(0.0);
    }

    @Test
    void getCart_cartDoesNotExist_createsCart() {

        User user = buildUser();

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.empty());

        when(cartRepository.save(any(Cart.class)))
            .thenAnswer(invocation -> {
                Cart cart = invocation.getArgument(0);
                cart.setId(10L);
                return cart;
            });

        when(
            cartItemRepository
                .findByCartIdOrderByIdAsc(10L)
        ).thenReturn(List.of());

        CartResponseDTO result =
            cartService.getCart(1L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(1L);

        verify(cartRepository)
            .save(any(Cart.class));
    }

    @Test
    void addItem_newProductWithStock_savesItem() {

        User user = buildUser();
        Cart cart = buildCart(user);
        Product product = buildPhysicalProduct(5);

        CartItemRequestDTO request =
            buildRequest(20L, 2);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(productRepository.findById(20L))
            .thenReturn(Optional.of(product));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdAndProductId(10L, 20L)
        ).thenReturn(Optional.empty());

        when(
            cartItemRepository
                .findByCartIdOrderByIdAsc(10L)
        ).thenReturn(List.of());

        cartService.addItem(1L, request);

        ArgumentCaptor<CartItem> captor =
            ArgumentCaptor.forClass(CartItem.class);

        verify(cartItemRepository)
            .save(captor.capture());

        CartItem savedItem = captor.getValue();

        assertThat(savedItem.getCart())
            .isEqualTo(cart);

        assertThat(savedItem.getProduct())
            .isEqualTo(product);

        assertThat(savedItem.getQuantity())
            .isEqualTo(2);

        // The stock should not be modified when adding an item to the cart
        assertThat(product.getStock())
            .isEqualTo(5);

        verify(productRepository, never())
            .save(any(Product.class));
    }

    @Test
    void addItem_existingProduct_increasesQuantity() {

        User user = buildUser();
        Cart cart = buildCart(user);
        Product product = buildPhysicalProduct(5);

        CartItem existingItem = new CartItem();
        existingItem.setId(30L);
        existingItem.setCart(cart);
        existingItem.setProduct(product);
        existingItem.setQuantity(3);

        CartItemRequestDTO request =
            buildRequest(20L, 2);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(productRepository.findById(20L))
            .thenReturn(Optional.of(product));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdAndProductId(10L, 20L)
        ).thenReturn(Optional.of(existingItem));

        when(
            cartItemRepository
                .findByCartIdOrderByIdAsc(10L)
        ).thenReturn(List.of(existingItem));

        CartResponseDTO result =
            cartService.addItem(1L, request);

        assertThat(existingItem.getQuantity())
            .isEqualTo(5);

        assertThat(result.getItems())
            .hasSize(1);

        assertThat(result.getItems().get(0).getQuantity())
            .isEqualTo(5);

        assertThat(result.getTotal())
            .isEqualTo(5000.0);

        verify(cartItemRepository)
            .save(existingItem);
    }

    @Test
    void addItem_quantityExceedsStock_throws() {

        User user = buildUser();
        Cart cart = buildCart(user);
        Product product = buildPhysicalProduct(5);

        CartItem existingItem = new CartItem();
        existingItem.setId(30L);
        existingItem.setCart(cart);
        existingItem.setProduct(product);
        existingItem.setQuantity(4);

        CartItemRequestDTO request =
            buildRequest(20L, 2);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(productRepository.findById(20L))
            .thenReturn(Optional.of(product));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdAndProductId(10L, 20L)
        ).thenReturn(Optional.of(existingItem));

        assertThatThrownBy(
            () -> cartService.addItem(1L, request)
        )
            .isInstanceOf(
                InsufficientStockException.class
            )
            .hasMessageContaining("6")
            .hasMessageContaining("5");

        verify(cartItemRepository, never())
            .save(any(CartItem.class));

        assertThat(product.getStock())
            .isEqualTo(5);
    }

    @Test
    void addItem_zeroQuantity_throws() {

        CartItemRequestDTO request =
            buildRequest(20L, 0);

        assertThatThrownBy(
            () -> cartService.addItem(1L, request)
        )
            .isInstanceOf(
                InvalidQuantityException.class
            );

        verifyNoInteractions(
            userRepository,
            productRepository,
            cartRepository,
            cartItemRepository
        );
    }

    @Test
    void addItem_negativeQuantity_throws() {

        CartItemRequestDTO request =
            buildRequest(20L, -2);

        assertThatThrownBy(
            () -> cartService.addItem(1L, request)
        )
            .isInstanceOf(
                InvalidQuantityException.class
            );
    }

    @Test
    void addItem_serviceProduct_doesNotValidateStock() {

        User user = buildUser();
        Cart cart = buildCart(user);

        Product service = new Product();
        service.setId(20L);
        service.setName("Clase particular");
        service.setPrice(5000.0);
        service.setType(ProductType.SERVICE);
        service.setStock(null);

        CartItemRequestDTO request =
            buildRequest(20L, 3);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(productRepository.findById(20L))
            .thenReturn(Optional.of(service));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdAndProductId(10L, 20L)
        ).thenReturn(Optional.empty());

        when(
            cartItemRepository
                .findByCartIdOrderByIdAsc(10L)
        ).thenReturn(List.of());

        cartService.addItem(1L, request);

        verify(cartItemRepository)
            .save(any(CartItem.class));
    }

    @Test
    void removeItem_existingItem_deletes() {

        User user = buildUser();
        Cart cart = buildCart(user);

        Product product = buildPhysicalProduct(5);

        CartItem item = new CartItem();
        item.setId(30L);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(2);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdAndProductId(10L, 20L)
        ).thenReturn(Optional.of(item));

        cartService.removeItem(1L, 20L);

        verify(cartItemRepository)
            .delete(item);
    }

    @Test
    void clearCart_existingCart_deletesAllItems() {

        User user = buildUser();
        Cart cart = buildCart(user);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        cartService.clearCart(1L);

        verify(cartItemRepository)
            .deleteByCartId(10L);
    }
}