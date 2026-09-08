package com.uade.e_commerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    // EnumType.STRING stores "PHYSICAL"/"SERVICE" as text in the database,
    // instead of 0/1. That way, if we add a third type down the line, the
    // already-saved data doesn't break (it would with ORDINAL).

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;

    private Integer stock;

    private Integer minutesDuration;

    private String attendanceType;

    // Each product belongs to a single category (required).
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // The user who posted the product (the "seller"). Required: every
    // product has to have an owner.
    @ManyToOne
    @JoinColumn(name = "publisher_id", nullable = false)
    private User publisher;
}