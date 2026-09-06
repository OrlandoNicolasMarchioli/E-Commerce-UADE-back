package com.uade.e_commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uade.e_commerce.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Las más nuevas primero, que es como se muestran en la ficha del producto.
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    // Para el historial de reseñas de un usuario.
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Lo usamos para cortar el POST antes de que la base tire el error
    // de la restricción única y podamos devolver un 409 prolijo.
    boolean existsByProductIdAndUserId(Long productId, Long userId);
}
