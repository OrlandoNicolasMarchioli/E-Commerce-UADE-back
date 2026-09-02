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
 * Categoría de un producto (ej: "Útiles", "Cursos", "Clases particulares").
 * Un producto pertenece a una única categoría (relación 1 a muchos).
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

    // unique=true porque no tiene sentido tener dos categorías con el mismo nombre
    @Column(name = "category_name", nullable = false, unique = true)
    private String name;

    private String description;
}