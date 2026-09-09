package com.uade.e_commerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(
        Long cartId,
        Long productId
    );

    List<CartItem> findByCartIdOrderByIdAsc(Long cartId);

    void deleteByCartId(Long cartId);
}