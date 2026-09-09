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

import com.uade.e_commerce.dto.order.OrderResponseDTO;
import com.uade.e_commerce.exception.EmptyCartException;
import com.uade.e_commerce.exception.InsufficientStockException;
import com.uade.e_commerce.exception.InvalidOrderStateException;
import com.uade.e_commerce.exception.OrderNotFoundException;
import com.uade.e_commerce.exception.UserNotFoundException;
import com.uade.e_commerce.model.Cart;
import com.uade.e_commerce.model.CartItem;
import com.uade.e_commerce.model.Order;
import com.uade.e_commerce.model.OrderItem;
import com.uade.e_commerce.model.OrderStatus;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductType;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.repository.CartItemRepository;
import com.uade.e_commerce.repository.CartRepository;
import com.uade.e_commerce.repository.OrderItemRepository;
import com.uade.e_commerce.repository.OrderRepository;
import com.uade.e_commerce.repository.ProductRepository;
import com.uade.e_commerce.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

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

    private Product buildServiceProduct() {
        Product product = new Product();
        product.setId(30L);
        product.setName("Clase particular");
        product.setPrice(5000.0);
        product.setType(ProductType.SERVICE);
        product.setStock(null);
        return product;
    }

    private CartItem buildCartItem(
        Cart cart,
        Product product,
        Integer quantity
    ) {
        CartItem cartItem = new CartItem();
        cartItem.setId(50L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    private Order buildOrder(
        User user,
        OrderStatus status
    ) {
        Order order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setStatus(status);
        order.setTotal(2000.0);
        return order;
    }

    private OrderItem buildOrderItem(
        Product product,
        Integer quantity,
        Double unitPrice
    ) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(200L);
        orderItem.setProduct(product);
        orderItem.setProductName(product.getName());
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(unitPrice);
        return orderItem;
    }

    // Makes save() behave like the database: it returns the same order it
    // received, already with an id.
    private void stubOrderSave() {
        when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(100L);
                return order;
            });
    }

    // =========================
    // CHECKOUT
    // =========================

    @Test
    void checkout_validCart_createsPendingOrderWithTotal() {

        User user = buildUser();
        Cart cart = buildCart(user);
        Product product = buildPhysicalProduct(10);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdOrderByIdAsc(10L)
        ).thenReturn(
            List.of(buildCartItem(cart, product, 2))
        );

        stubOrderSave();

        OrderResponseDTO result =
            orderService.checkout(1L);

        ArgumentCaptor<Order> captor =
            ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(captor.capture());

        Order savedOrder = captor.getValue();

        assertThat(savedOrder.getStatus())
            .isEqualTo(OrderStatus.PENDING);

        assertThat(savedOrder.getTotal())
            .isEqualTo(2000.0);

        assertThat(savedOrder.getItems())
            .hasSize(1);

        assertThat(savedOrder.getOrderDate())
            .isNotNull();

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getTotal()).isEqualTo(2000.0);

        assertThat(result.getStatus())
            .isEqualTo("PENDING");
    }

    @Test
    void checkout_copiesNameAndPriceIntoTheOrderItem() {

        User user = buildUser();
        Cart cart = buildCart(user);
        Product product = buildPhysicalProduct(10);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdOrderByIdAsc(10L)
        ).thenReturn(
            List.of(buildCartItem(cart, product, 3))
        );

        stubOrderSave();

        orderService.checkout(1L);

        ArgumentCaptor<Order> captor =
            ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(captor.capture());

        OrderItem savedItem =
            captor.getValue().getItems().get(0);

        // The price travels as a copy: this is what makes the order stop
        // depending on later catalog changes.
        assertThat(savedItem.getUnitPrice())
            .isEqualTo(1000.0);

        assertThat(savedItem.getProductName())
            .isEqualTo("Cuaderno");

        assertThat(savedItem.getQuantity())
            .isEqualTo(3);

        // addItem() has to leave both sides of the relationship linked.
        assertThat(savedItem.getOrder())
            .isSameAs(captor.getValue());
    }

    @Test
    void checkout_physicalProduct_discountsStock() {

        User user = buildUser();
        Cart cart = buildCart(user);
        Product product = buildPhysicalProduct(10);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdOrderByIdAsc(10L)
        ).thenReturn(
            List.of(buildCartItem(cart, product, 4))
        );

        stubOrderSave();

        orderService.checkout(1L);

        assertThat(product.getStock()).isEqualTo(6);

        verify(productRepository).save(product);
    }

    @Test
    void checkout_serviceProduct_doesNotTouchStock() {

        User user = buildUser();
        Cart cart = buildCart(user);
        Product product = buildServiceProduct();

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdOrderByIdAsc(10L)
        ).thenReturn(
            List.of(buildCartItem(cart, product, 2))
        );

        stubOrderSave();

        OrderResponseDTO result =
            orderService.checkout(1L);

        assertThat(product.getStock()).isNull();

        assertThat(result.getTotal())
            .isEqualTo(10000.0);

        verify(productRepository, never())
            .save(any(Product.class));
    }

    @Test
    void checkout_insufficientStock_doesNotCreateOrder() {

        User user = buildUser();
        Cart cart = buildCart(user);
        Product product = buildPhysicalProduct(1);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdOrderByIdAsc(10L)
        ).thenReturn(
            List.of(buildCartItem(cart, product, 5))
        );

        assertThatThrownBy(() ->
            orderService.checkout(1L)
        ).isInstanceOf(
            InsufficientStockException.class
        );

        // Nothing gets saved and stock stays untouched: validation happens
        // before any change is applied.
        assertThat(product.getStock()).isEqualTo(1);

        verify(orderRepository, never())
            .save(any(Order.class));

        verify(productRepository, never())
            .save(any(Product.class));

        verify(cartItemRepository, never())
            .deleteByCartId(any());
    }

    @Test
    void checkout_oneItemWithoutStock_doesNotDiscountTheOthers() {

        User user = buildUser();
        Cart cart = buildCart(user);

        Product available = buildPhysicalProduct(10);

        Product outOfStock = new Product();
        outOfStock.setId(21L);
        outOfStock.setName("Mochila");
        outOfStock.setPrice(3000.0);
        outOfStock.setType(ProductType.PHYSICAL);
        outOfStock.setStock(0);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdOrderByIdAsc(10L)
        ).thenReturn(
            List.of(
                buildCartItem(cart, available, 1),
                buildCartItem(cart, outOfStock, 1)
            )
        );

        assertThatThrownBy(() ->
            orderService.checkout(1L)
        ).isInstanceOf(
            InsufficientStockException.class
        );

        // The first product keeps its stock even though it was valid: the
        // whole cart is checked before touching anything.
        assertThat(available.getStock())
            .isEqualTo(10);

        verify(productRepository, never())
            .save(any(Product.class));
    }

    @Test
    void checkout_nullStockPhysicalProduct_throws() {

        User user = buildUser();
        Cart cart = buildCart(user);
        Product product = buildPhysicalProduct(null);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdOrderByIdAsc(10L)
        ).thenReturn(
            List.of(buildCartItem(cart, product, 1))
        );

        assertThatThrownBy(() ->
            orderService.checkout(1L)
        ).isInstanceOf(
            InsufficientStockException.class
        );
    }

    @Test
    void checkout_emptiesTheCart() {

        User user = buildUser();
        Cart cart = buildCart(user);
        Product product = buildPhysicalProduct(10);

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.of(cart));

        when(
            cartItemRepository
                .findByCartIdOrderByIdAsc(10L)
        ).thenReturn(
            List.of(buildCartItem(cart, product, 1))
        );

        stubOrderSave();

        orderService.checkout(1L);

        verify(cartItemRepository)
            .deleteByCartId(10L);
    }

    @Test
    void checkout_emptyCart_throws() {

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

        assertThatThrownBy(() ->
            orderService.checkout(1L)
        ).isInstanceOf(EmptyCartException.class);

        verify(orderRepository, never())
            .save(any(Order.class));
    }

    @Test
    void checkout_cartDoesNotExist_throws() {

        User user = buildUser();

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(cartRepository.findByUserId(1L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            orderService.checkout(1L)
        ).isInstanceOf(EmptyCartException.class);
    }

    @Test
    void checkout_userNotFound_throws() {

        when(userRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            orderService.checkout(99L)
        ).isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(orderRepository);
    }

    // =========================
    // READS
    // =========================

    @Test
    void getOrdersByUser_returnsHistory() {

        User user = buildUser();

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(
            orderRepository
                .findByUserIdOrderByOrderDateDesc(1L)
        ).thenReturn(
            List.of(
                buildOrder(user, OrderStatus.PENDING)
            )
        );

        List<OrderResponseDTO> result =
            orderService.getOrdersByUser(1L);

        assertThat(result).hasSize(1);

        assertThat(result.get(0).getId())
            .isEqualTo(100L);

        assertThat(result.get(0).getStatus())
            .isEqualTo("PENDING");
    }

    @Test
    void getOrderById_returnsItemsWithFrozenPrice() {

        User user = buildUser();
        Product product = buildPhysicalProduct(10);

        Order order =
            buildOrder(user, OrderStatus.PENDING);

        when(orderRepository.findById(100L))
            .thenReturn(Optional.of(order));

        // The item was bought at 800, even though the product is worth 1000
        // in the catalog today.
        when(
            orderItemRepository
                .findByOrderIdOrderByIdAsc(100L)
        ).thenReturn(
            List.of(
                buildOrderItem(product, 2, 800.0)
            )
        );

        OrderResponseDTO result =
            orderService.getOrderById(100L);

        assertThat(result.getItems()).hasSize(1);

        assertThat(
            result.getItems().get(0).getUnitPrice()
        ).isEqualTo(800.0);

        assertThat(
            result.getItems().get(0).getSubtotal()
        ).isEqualTo(1600.0);
    }

    @Test
    void getOrderById_notFound_throws() {

        when(orderRepository.findById(404L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            orderService.getOrderById(404L)
        ).isInstanceOf(OrderNotFoundException.class);
    }

    // =========================
    // STATUS
    // =========================

    @Test
    void updateStatus_pendingToPaid_updates() {

        User user = buildUser();

        Order order =
            buildOrder(user, OrderStatus.PENDING);

        when(orderRepository.findById(100L))
            .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
            .thenReturn(order);

        OrderResponseDTO result =
            orderService.updateStatus(100L, "PAID");

        assertThat(order.getStatus())
            .isEqualTo(OrderStatus.PAID);

        assertThat(result.getStatus())
            .isEqualTo("PAID");
    }

    @Test
    void updateStatus_acceptsLowercase() {

        User user = buildUser();

        Order order =
            buildOrder(user, OrderStatus.PENDING);

        when(orderRepository.findById(100L))
            .thenReturn(Optional.of(order));

        when(orderRepository.save(order))
            .thenReturn(order);

        orderService.updateStatus(100L, "paid");

        assertThat(order.getStatus())
            .isEqualTo(OrderStatus.PAID);
    }

    @Test
    void updateStatus_skippingAStep_throws() {

        User user = buildUser();

        Order order =
            buildOrder(user, OrderStatus.PENDING);

        when(orderRepository.findById(100L))
            .thenReturn(Optional.of(order));

        // An order that hasn't been paid can't be shipped.
        assertThatThrownBy(() ->
            orderService.updateStatus(100L, "SHIPPED")
        ).isInstanceOf(
            InvalidOrderStateException.class
        );

        assertThat(order.getStatus())
            .isEqualTo(OrderStatus.PENDING);

        verify(orderRepository, never())
            .save(any(Order.class));
    }

    @Test
    void updateStatus_goingBackwards_throws() {

        User user = buildUser();

        Order order =
            buildOrder(user, OrderStatus.DELIVERED);

        when(orderRepository.findById(100L))
            .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
            orderService.updateStatus(100L, "PENDING")
        ).isInstanceOf(
            InvalidOrderStateException.class
        );
    }

    @Test
    void updateStatus_unknownValue_throws() {

        User user = buildUser();

        Order order =
            buildOrder(user, OrderStatus.PENDING);

        when(orderRepository.findById(100L))
            .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
            orderService.updateStatus(100L, "REGALADO")
        ).isInstanceOf(
            InvalidOrderStateException.class
        );
    }

    @Test
    void updateStatus_nullValue_throws() {

        User user = buildUser();

        Order order =
            buildOrder(user, OrderStatus.PENDING);

        when(orderRepository.findById(100L))
            .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
            orderService.updateStatus(100L, null)
        ).isInstanceOf(
            InvalidOrderStateException.class
        );
    }

    @Test
    void updateStatus_cancel_restoresStock() {

        User user = buildUser();
        Product product = buildPhysicalProduct(6);

        Order order =
            buildOrder(user, OrderStatus.PENDING);

        when(orderRepository.findById(100L))
            .thenReturn(Optional.of(order));

        when(
            orderItemRepository
                .findByOrderIdOrderByIdAsc(100L)
        ).thenReturn(
            List.of(
                buildOrderItem(product, 4, 1000.0)
            )
        );

        when(orderRepository.save(order))
            .thenReturn(order);

        orderService.updateStatus(
            100L,
            "CANCELLED"
        );

        // The 4 units the purchase had taken go back to the catalog.
        assertThat(product.getStock())
            .isEqualTo(10);

        assertThat(order.getStatus())
            .isEqualTo(OrderStatus.CANCELLED);

        verify(productRepository).save(product);
    }

    @Test
    void updateStatus_cancelServiceProduct_doesNotTouchStock() {

        User user = buildUser();
        Product product = buildServiceProduct();

        Order order =
            buildOrder(user, OrderStatus.PENDING);

        when(orderRepository.findById(100L))
            .thenReturn(Optional.of(order));

        when(
            orderItemRepository
                .findByOrderIdOrderByIdAsc(100L)
        ).thenReturn(
            List.of(
                buildOrderItem(product, 2, 5000.0)
            )
        );

        when(orderRepository.save(order))
            .thenReturn(order);

        orderService.updateStatus(
            100L,
            "CANCELLED"
        );

        assertThat(product.getStock()).isNull();

        verify(productRepository, never())
            .save(any(Product.class));
    }

    @Test
    void updateStatus_cancelAlreadyShippedOrder_throws() {

        User user = buildUser();
        Product product = buildPhysicalProduct(6);

        Order order =
            buildOrder(user, OrderStatus.SHIPPED);

        when(orderRepository.findById(100L))
            .thenReturn(Optional.of(order));

        // Once shipped the stock already left, so it can't be given back.
        assertThatThrownBy(() ->
            orderService.updateStatus(
                100L,
                "CANCELLED"
            )
        ).isInstanceOf(
            InvalidOrderStateException.class
        );

        assertThat(product.getStock()).isEqualTo(6);

        verify(productRepository, never())
            .save(any(Product.class));
    }

    @Test
    void updateStatus_sameStatus_throws() {

        User user = buildUser();

        Order order =
            buildOrder(user, OrderStatus.PENDING);

        when(orderRepository.findById(100L))
            .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
            orderService.updateStatus(100L, "PENDING")
        ).isInstanceOf(
            InvalidOrderStateException.class
        );
    }

    @Test
    void updateStatus_orderNotFound_throws() {

        when(orderRepository.findById(404L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            orderService.updateStatus(404L, "PAID")
        ).isInstanceOf(OrderNotFoundException.class);
    }
}
