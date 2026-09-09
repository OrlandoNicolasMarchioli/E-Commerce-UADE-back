package com.uade.e_commerce.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;

import com.uade.e_commerce.model.Category;
import com.uade.e_commerce.model.Order;
import com.uade.e_commerce.model.OrderItem;
import com.uade.e_commerce.model.OrderStatus;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductType;
import com.uade.e_commerce.model.User;

@SpringBootTest(
    webEnvironment = WebEnvironment.NONE
)
@Transactional
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;

    private Product product;

    @BeforeEach
    void setUp() {

        user = userRepository.save(
            new User(
                null,
                "Benjamin",
                "Moyano",
                "benjamin@test.com",
                "hash",
                "1225238",
                LocalDateTime.now(),
                true
            )
        );

        Category category =
            categoryRepository.save(
                new Category(
                    null,
                    "Utiles",
                    "Utiles escolares"
                )
            );

        Product newProduct = new Product();

        newProduct.setName("Cuaderno");
        newProduct.setDescription("Cuaderno A4");
        newProduct.setPrice(new BigDecimal("1000.00"));
        newProduct.setType(ProductType.PHYSICAL);
        newProduct.setStock(10);
        newProduct.setCategory(category);
        newProduct.setPublisher(user);

        product = productRepository.save(newProduct);
    }

    private Order buildOrder(
        LocalDateTime date,
        BigDecimal total
    ) {

        Order order = new Order();

        order.setUser(user);
        order.setOrderDate(date);
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(total);

        return order;
    }

    private OrderItem buildOrderItem(
        Integer quantity,
        BigDecimal unitPrice
    ) {

        OrderItem orderItem = new OrderItem();

        orderItem.setProduct(product);
        orderItem.setProductName(product.getName());
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(unitPrice);

        return orderItem;
    }

    // Checks that saving the order also saves its lines, without having to
    // save each OrderItem by hand (CascadeType.ALL).
    @Test
    void save_cascadesItems() {

        Order order = buildOrder(
            LocalDateTime.now(),
            new BigDecimal("3000.00")
        );

        order.addItem(buildOrderItem(2, new BigDecimal("1000.00")));
        order.addItem(buildOrderItem(1, new BigDecimal("1000.00")));

        Order saved = orderRepository.save(order);

        List<OrderItem> items =
            orderItemRepository
                .findByOrderIdOrderByIdAsc(
                    saved.getId()
                );

        assertThat(saved.getId()).isNotNull();
        assertThat(items).hasSize(2);

        assertThat(items.get(0).getOrder().getId())
            .isEqualTo(saved.getId());

        assertThat(items.get(0).getQuantity())
            .isEqualTo(2);
    }

    // The frozen price has to survive a round trip to the database, and stay
    // independent from the current price of the product.
    @Test
    void save_keepsTheFrozenPrice() {

        Order order = buildOrder(
            LocalDateTime.now(),
            new BigDecimal("1600.00")
        );

        order.addItem(buildOrderItem(2, new BigDecimal("800.00")));

        Order saved = orderRepository.save(order);

        product.setPrice(new BigDecimal("2500.00"));
        productRepository.save(product);

        OrderItem item =
            orderItemRepository
                .findByOrderIdOrderByIdAsc(
                    saved.getId()
                )
                .get(0);

        assertThat(item.getUnitPrice())
            .isEqualByComparingTo("800.00");

        assertThat(item.getProductName())
            .isEqualTo("Cuaderno");
    }

    @Test
    void findByUserId_returnsNewestFirst() {

        orderRepository.save(
            buildOrder(
                LocalDateTime.now().minusDays(2),
                new BigDecimal("1000.00")
            )
        );

        orderRepository.save(
            buildOrder(
                LocalDateTime.now(),
                new BigDecimal("2000.00")
            )
        );

        List<Order> result =
            orderRepository
                .findByUserIdOrderByOrderDateDesc(
                    user.getId()
                );

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getTotal())
            .isEqualByComparingTo("2000.00");

        assertThat(result.get(1).getTotal())
            .isEqualByComparingTo("1000.00");
    }

    // The JOIN FETCH has to bring the order together with its items in a
    // single query, and DISTINCT has to prevent the order from appearing
    // repeated once per item.
    @Test
    void findByUserIdWithItems_doesNotRepeatTheOrder() {

        Order order = buildOrder(
            LocalDateTime.now(),
            new BigDecimal("3000.00")
        );

        order.addItem(buildOrderItem(2, new BigDecimal("1000.00")));
        order.addItem(buildOrderItem(1, new BigDecimal("1000.00")));

        orderRepository.save(order);

        List<Order> result =
            orderRepository.findByUserIdWithItems(
                user.getId()
            );

        assertThat(result).hasSize(1);

        assertThat(result.get(0).getItems())
            .hasSize(2);
    }

    @Test
    void findByUserIdWithItems_returnsNewestFirst() {

        orderRepository.save(
            buildOrder(
                LocalDateTime.now().minusDays(2),
                new BigDecimal("1000.00")
            )
        );

        orderRepository.save(
            buildOrder(
                LocalDateTime.now(),
                new BigDecimal("2000.00")
            )
        );

        List<Order> result =
            orderRepository.findByUserIdWithItems(
                user.getId()
            );

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getTotal())
            .isEqualByComparingTo("2000.00");
    }

    @Test
    void findByIdWithItems_returnsOrderWithItems() {

        Order order = buildOrder(
            LocalDateTime.now(),
            new BigDecimal("2000.00")
        );

        order.addItem(buildOrderItem(2, new BigDecimal("1000.00")));

        Order saved = orderRepository.save(order);

        var result =
            orderRepository.findByIdWithItems(
                saved.getId()
            );

        assertThat(result).isPresent();

        assertThat(result.get().getItems())
            .hasSize(1);

        assertThat(
            result.get().getItems().get(0).getProductName()
        ).isEqualTo("Cuaderno");
    }

    @Test
    void findByIdWithItems_notFound_returnsEmpty() {

        assertThat(
            orderRepository.findByIdWithItems(999999L)
        ).isEmpty();
    }

    @Test
    void findByUserId_userWithoutOrders_returnsEmpty() {

        List<Order> result =
            orderRepository
                .findByUserIdOrderByOrderDateDesc(
                    user.getId()
                );

        assertThat(result).isEmpty();
    }

    @Test
    void save_statusIsStoredAsText() {

        Order order = buildOrder(
            LocalDateTime.now(),
            new BigDecimal("1000.00")
        );

        order.setStatus(OrderStatus.DELIVERED);

        Order saved = orderRepository.save(order);

        assertThat(
            orderRepository
                .findById(saved.getId())
                .get()
                .getStatus()
        ).isEqualTo(OrderStatus.DELIVERED);
    }
}
