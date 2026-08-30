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
@Table(name = "productos")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    // EnumType.STRING guarda "FISICO"/"SERVICIO" como texto en la base,
    // en vez de 0/1. Así si el día de mañana agregamos un tercer tipo,
    // no se rompen los datos ya guardados (con ORDINAL sí pasaría).

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType type;

    private Integer stock;

    private Integer duracionMinutos;

    private String modalidad;

    // Cada producto pertenece a una sola categoría (obligatorio).    
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // El usuario que publicó el producto (el "vendedor"). Obligatorio:
    // todo producto tiene que tener un dueño.    
    @ManyToOne
    @JoinColumn(name = "publicador_id", nullable = false)
    private User publicador;
}