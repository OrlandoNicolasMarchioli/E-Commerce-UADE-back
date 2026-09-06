package com.uade.e_commerce.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reseña que un usuario deja sobre un producto: una calificación
 * numérica (1 a 5) y un comentario de texto.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "reviews",
    // Un usuario puede reseñar un producto una sola vez. Lo dejamos como
    // restricción de la BASE (y no solo como un if en el código) porque
    // así la base misma garantiza que nunca queden duplicados, aunque
    // llegue el mismo pedido dos veces al mismo tiempo.
    uniqueConstraints = @UniqueConstraint(
        name = "uk_review_product_user",
        columnNames = { "product_id", "user_id" }
    )
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    // El rango 1..5 no se puede expresar con una anotación simple de JPA,
    // así que la validación vive en el controller (ver ProductReviewController).
    @Column(nullable = false)
    private Integer rating;

    // columnDefinition = TEXT porque el VARCHAR(255) por defecto se queda
    // corto para un comentario largo.
    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // LAZY: al traer una reseña no queremos que Hibernate traiga también
    // el producto entero si no lo vamos a usar (mismo criterio que ProductImage).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // El autor de la reseña. Obligatorio: toda reseña tiene dueño.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
