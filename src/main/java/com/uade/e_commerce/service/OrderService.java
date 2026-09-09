package com.uade.e_commerce.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.uade.e_commerce.dto.order.OrderItemResponseDTO;
import com.uade.e_commerce.dto.order.OrderResponseDTO;
import com.uade.e_commerce.exception.EmptyCartException;
import com.uade.e_commerce.exception.InsufficientStockException;
import com.uade.e_commerce.exception.InvalidOrderStateException;
import com.uade.e_commerce.exception.OrderAccessDeniedException;
import com.uade.e_commerce.exception.OrderNotFoundException;
import com.uade.e_commerce.exception.ProductNotFoundException;
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
import com.uade.e_commerce.repository.OrderRepository;
import com.uade.e_commerce.repository.ProductRepository;
import com.uade.e_commerce.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

// Checkout is the operation that turns a cart into an order. @Transactional
// at class level means each public method runs as a single unit: if any step
// fails (missing stock, for instance), nothing gets saved. Without it a
// checkout could leave the stock already decreased and no order created.
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // Used to re-read a product with a lock. It's needed because a query
    // alone isn't enough: see lockProduct().
    private final EntityManager entityManager;

    public OrderService(
        OrderRepository orderRepository,
        CartRepository cartRepository,
        CartItemRepository cartItemRepository,
        ProductRepository productRepository,
        UserRepository userRepository,
        EntityManager entityManager
    ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    public OrderResponseDTO checkout(Long userId) {

        User user = getUser(userId);

        // The cart is read with a lock before anything else. Two
        // simultaneous checkouts from the same user get serialized here: the
        // second one only moves forward once the first has finished and
        // emptied the cart, so it doesn't create a duplicate order.
        Cart cart = cartRepository
            .findByUserIdForUpdate(userId)
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

        // The products are re-read with a lock before being looked at. From
        // here until the transaction ends, no other checkout can read or
        // modify their stock, so the value validated below is the same one
        // that gets discounted afterwards.
        Map<Long, Product> lockedProducts =
            lockProducts(cartItems);

        // First pass: check that every line can be fulfilled. Stock is only
        // touched once the whole cart is known to be valid, so a failure on
        // the last item doesn't leave the first ones already discounted.
        for (CartItem cartItem : cartItems) {

            validateStock(
                lockedProducts.get(
                    cartItem.getProduct().getId()
                ),
                cartItem.getQuantity()
            );
        }

        Order order = new Order();

        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        double total = 0.0;

        // Second pass: now the order is actually built and stock is applied.
        for (CartItem cartItem : cartItems) {

            Product product =
                lockedProducts.get(
                    cartItem.getProduct().getId()
                );

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

            applyStock(
                product,
                -cartItem.getQuantity()
            );
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

        // findByUserIdWithItems brings the items in the same query. Reading
        // them order by order would run one extra query per order.
        return orderRepository
            .findByUserIdWithItems(userId)
            .stream()
            .map(this::buildResponse)
            .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderById(
        Long orderId,
        Long userId
    ) {

        return buildResponse(
            getOrderOwnedBy(orderId, userId)
        );
    }

    public OrderResponseDTO updateStatus(
        Long orderId,
        Long userId,
        String newStatus
    ) {

        Order order = getOrderOwnedBy(orderId, userId);

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

    // An order can only be read or modified by the user who placed it.
    //
    // The id of the user travels as a parameter because the project has no
    // authentication yet: when there is one, it will come from the session
    // instead and this check stays the same.
    private Order getOrderOwnedBy(
        Long orderId,
        Long userId
    ) {

        Order order = getOrder(orderId);

        if (
            !order.getUser().getId().equals(userId)
        ) {
            throw new OrderAccessDeniedException(
                orderId,
                userId
            );
        }

        return order;
    }

    // The locks are taken ordered by product id so that two simultaneous
    // checkouts request them in the same order. Taking them in different
    // orders is what leaves two transactions waiting for each other forever
    // (a deadlock).
    private Map<Long, Product> lockProducts(
        List<CartItem> cartItems
    ) {

        Map<Long, Product> lockedProducts =
            new LinkedHashMap<>();

        cartItems
            .stream()
            .map(cartItem ->
                cartItem.getProduct().getId()
            )
            .distinct()
            .sorted()
            .forEach(productId ->
                lockedProducts.put(
                    productId,
                    lockProduct(productId)
                )
            );

        return lockedProducts;
    }

    // Reads the product blocking its row until the transaction ends
    // (SELECT ... FOR UPDATE), and above all refreshing what's in memory.
    //
    // The refresh is the important part. CartItem and OrderItem point at
    // Product with an EAGER @ManyToOne, so by this point the product is
    // already loaded in memory. If the lock were taken with a plain query,
    // JPA would return that already loaded copy and throw away the row it
    // just read: the stock would be validated and discounted against an
    // outdated value, which is exactly the problem the lock is meant to
    // avoid. refresh() overwrites the in-memory state with the one in the
    // database.
    private Product lockProduct(Long productId) {

        Product product = productRepository
            .findById(productId)
            .orElseThrow(() ->
                new ProductNotFoundException(
                    "Producto no encontrado con id: " +
                    productId
                )
            );

        entityManager.refresh(
            product,
            LockModeType.PESSIMISTIC_WRITE
        );

        return product;
    }

    // Services (lessons, courses) have no stock to control: they can be sold
    // as many times as needed. Same criteria already used by CartService.
    private void validateStock(
        Product product,
        Integer quantity
    ) {

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
        if (quantity > availableStock) {

            throw new InsufficientStockException(
                product.getId(),
                quantity,
                availableStock
            );
        }
    }

    // A negative amount discounts (a purchase) and a positive one gives back
    // (a cancellation). Both cases are the same operation, so they share the
    // check for products without stock.
    private void applyStock(
        Product product,
        int amount
    ) {

        if (product.getType() != ProductType.PHYSICAL) {
            return;
        }

        int currentStock =
            product.getStock() == null
                ? 0
                : product.getStock();

        product.setStock(currentStock + amount);

        productRepository.save(product);
    }

    private void restoreStock(Order order) {

        // The items are already loaded with the order, so they aren't asked
        // for again. The products, on the other hand, are re-read with a
        // lock: giving stock back also reads and writes it.
        for (OrderItem orderItem : order.getItems()) {

            Product product = lockProduct(
                orderItem.getProduct().getId()
            );

            applyStock(
                product,
                orderItem.getQuantity()
            );
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
            .findByIdWithItems(orderId)
            .orElseThrow(() ->
                new OrderNotFoundException(orderId)
            );
    }

    private OrderResponseDTO buildResponse(Order order) {

        // The items come already loaded with the order, so building the
        // response doesn't hit the database again.
        List<OrderItemResponseDTO> items =
            order
                .getItems()
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
