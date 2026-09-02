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

    // EnumType.STRING guarda "PHYSICAL"/"SERVICE" como texto en la base,
    // en vez de 0/1. Así si el día de mañana agregamos un tercer tipo,
    // no se rompen los datos ya guardados (con ORDINAL sí pasaría).

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;

    private Integer stock;

    private Integer minutesDuration;

    private String attendanceType;

    // Cada producto pertenece a una sola categoría (obligatorio).    
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // El usuario que publicó el producto (el "vendedor"). Obligatorio:
    // todo producto tiene que tener un dueño.    
    @ManyToOne
    @JoinColumn(name = "publisher_id", nullable = false)
    private User publisher;
}