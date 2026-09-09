package com.uade.e_commerce.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * An order placed by a user. It's built from the contents of the cart at
 * checkout time and, from that point on, it stops depending on the cart:
 * it keeps its own copy of what was bought and at what price.
 */

// The table is called "orders" and not "order" because ORDER is a reserved
// SQL word (ORDER BY); Hibernate would fail to create the table.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    // A user has many orders (their purchase history), so ManyToOne here,
    // unlike Cart which is OneToOne (a single active cart per user).
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    // Same criteria as ProductType: STRING so the saved data doesn't break
    // if a new state is added in the middle of the enum later on.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    // The total is persisted instead of being recalculated like in the cart,
    // because it's the amount actually charged. It also lets the order
    // history be listed without loading every item of every order.
    @Column(nullable = false)
    private Double total;

    // CascadeType.ALL lets the whole order be saved with a single save():
    // the items go along with it. orphanRemoval means an item taken out of
    // the list is deleted from the database, since an item has no meaning
    // outside its order.
    //
    // @OrderBy keeps the lines always in the same order, so the response of
    // an order doesn't change between calls.
    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("id ASC")
    @ToString.Exclude
    private List<OrderItem> items = new ArrayList<>();

    // Both sides of the relationship are set in one place. If only
    // items.add() were called, the item would be left without its order and
    // the FK would come out null when saving.
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
