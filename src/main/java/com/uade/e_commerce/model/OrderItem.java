package com.uade.e_commerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * One line of an order: which product was bought, how many units, and at
 * what price. Unlike CartItem, it doesn't read the price from Product: it
 * keeps its own copy, frozen at the moment of purchase.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    // Excluded from toString() and equals() to break the cycle with Order:
    // Order prints its items, and each item would print its order again,
    // looping forever (StackOverflowError).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // A copy of the name at purchase time. If the product gets renamed
    // later, the old order still shows what the user actually bought.
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    // The price is frozen here. This is the difference that matters against
    // CartItem: the cart follows the catalog, the order doesn't.
    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;
}
