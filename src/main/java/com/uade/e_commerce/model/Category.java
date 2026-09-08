package com.uade.e_commerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A product's category (e.g. "Supplies", "Courses", "Private lessons").
 * A product belongs to a single category (1-to-many relationship).
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    // unique=true because it doesn't make sense to have two categories with
    // the same name
    @Column(name = "category_name", nullable = false, unique = true)
    private String name;

    private String description;
}