package com.uade.e_commerce.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.order.OrderItemResponseDTO;
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

import jakarta.transaction.Transactional;

// Checkout is the operation that turns a cart into an order. @Transactional
// at class level means each public method runs as a single unit: if any step
// fails (missing stock, for instance), nothing gets saved. Without it a
// checkout could leave the stock already decreased and no order created.
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
        OrderRepository orderRepository,
        OrderItemRepository orderItemRepository,
        CartRepository cartRepository,
        CartItemRepository cartItemRepository,
        ProductRepository productRepository,
        UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public OrderResponseDTO checkout(Long userId) {

        User user = getUser(userId);

        Cart cart = cartRepository
            .findByUserId(userId)
            .orElseThrow(() ->
                new EmptyCartException(userId)
            );

        List<CartItem> cartItems =
            cartItemRepository
                .findByCartIdOrderByIdAsc(
                    cart.getId()
                );

        // An order with no lines has nothing to charge, so it isn't created.
        if (cartItems.isEmpty()) {
            throw new EmptyCartException(userId);
        }

        // First pass: check that every line can be fulfilled. Stock is only
        // touched once the whole cart is known to be valid, so a failure on
        // the last item doesn't leave the first ones already discounted.
        for (CartItem cartItem : cartItems) {
            validateStock(cartItem);
        }

        Order order = new Order();

        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        double total = 0.0;

        // Second pass: now the order is actually built and stock is applied.
        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            OrderItem orderItem = new OrderItem();

            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());

            // The name and the price are copied instead of referenced: from
            // here on the order no longer follows changes in the catalog.
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());

            order.addItem(orderItem);

            total +=
                product.getPrice() * cartItem.getQuantity();

            discountStock(product, cartItem.getQuantity());
        }

        order.setTotal(total);

        // Saving the order also saves its items, thanks to CascadeType.ALL.
        Order savedOrder = orderRepository.save(order);

        // The cart is emptied because its content already became an order.
        // Leaving it as is would let the same purchase be placed twice.
        cartItemRepository.deleteByCartId(cart.getId());

        return buildResponse(savedOrder);
    }

    // Purchase history of a user.
    public List<OrderResponseDTO> getOrdersByUser(Long userId) {

        getUser(userId);

        return orderRepository
            .findByUserIdOrderByOrderDateDesc(userId)
            .stream()
            .map(this::buildResponse)
            .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderById(Long orderId) {

        return buildResponse(getOrder(orderId));
    }

    public OrderResponseDTO updateStatus(
        Long orderId,
        String newStatus
    ) {

        Order order = getOrder(orderId);

        OrderStatus target = parseStatus(newStatus);

        // The enum is the one that knows whether the transition makes sense.
        if (!order.getStatus().canTransitionTo(target)) {

            throw new InvalidOrderStateException(
                order.getStatus().name(),
                "cambiar a " + target.name()
            );
        }

        // Cancelling returns to the catalog what the purchase had taken. If
        // this didn't happen, every cancelled order would make stock
        // disappear for good.
        if (target == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(target);

        return buildResponse(
            orderRepository.save(order)
        );
    }

    // Services (lessons, courses) have no stock to control: they can be sold
    // as many times as needed. Same criteria already used by CartService.
    private void validateStock(CartItem cartItem) {

        Product product = cartItem.getProduct();

        if (product.getType() != ProductType.PHYSICAL) {
            return;
        }

        int availableStock =
            product.getStock() == null
                ? 0
                : product.getStock();

        // The cart already validated stock when the item was added, but time
        // may have passed and another user may have taken those units. The
        // check that counts is this one.
        if (cartItem.getQuantity() > availableStock) {

            throw new InsufficientStockException(
                product.getId(),
                cartItem.getQuantity(),
                availableStock
            );
        }
    }

    private void discountStock(
        Product product,
        Integer quantity
    ) {

        if (product.getType() != ProductType.PHYSICAL) {
            return;
        }

        product.setStock(
            product.getStock() - quantity
        );

        productRepository.save(product);
    }

    private void restoreStock(Order order) {

        List<OrderItem> orderItems =
            orderItemRepository
                .findByOrderIdOrderByIdAsc(
                    order.getId()
                );

        for (OrderItem orderItem : orderItems) {

            Product product = orderItem.getProduct();

            if (product.getType() != ProductType.PHYSICAL) {
                continue;
            }

            int currentStock =
                product.getStock() == null
                    ? 0
                    : product.getStock();

            product.setStock(
                currentStock + orderItem.getQuantity()
            );

            productRepository.save(product);
        }
    }

    // Unknown text is a client error, not a server one, so it comes out as a
    // 400 listing the accepted values instead of an opaque 500.
    private OrderStatus parseStatus(String status) {

        if (status == null || status.isBlank()) {

            throw new InvalidOrderStateException(
                "El estado del pedido es obligatorio"
            );
        }

        try {

            return OrderStatus.valueOf(
                status.trim().toUpperCase()
            );

        } catch (IllegalArgumentException ex) {

            throw new InvalidOrderStateException(
                "Estado de pedido inválido: " +
                    status +
                    ". Valores permitidos: " +
                    List.of(OrderStatus.values())
            );
        }
    }

    private User getUser(Long userId) {

        return userRepository
            .findById(userId)
            .orElseThrow(() ->
                new UserNotFoundException(userId)
            );
    }

    private Order getOrder(Long orderId) {

        return orderRepository
            .findById(orderId)
            .orElseThrow(() ->
                new OrderNotFoundException(orderId)
            );
    }

    private OrderResponseDTO buildResponse(Order order) {

        List<OrderItemResponseDTO> items =
            orderItemRepository
                .findByOrderIdOrderByIdAsc(
                    order.getId()
                )
                .stream()
                .map(OrderItemResponseDTO::fromEntity)
                .collect(Collectors.toList());

        // The total isn't recalculated here: the stored one is returned,
        // because it's the amount that was charged at purchase time.
        return new OrderResponseDTO(
            order.getId(),
            order.getUser().getId(),
            order.getOrderDate(),
            order.getStatus().name(),
            items,
            order.getTotal()
        );
    }
}
