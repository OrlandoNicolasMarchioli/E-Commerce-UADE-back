package com.uade.e_commerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.e_commerce.model.Cart;

import jakarta.persistence.LockModeType;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    // Reads the cart blocking its row until the transaction ends. It's used
    // by the checkout so that two simultaneous confirmations from the same
    // user (a double click on "buy") don't both build an order from the same
    // items: the second one waits, and by then the cart is already empty.
    //
    // Only the checkout needs it; browsing the cart keeps using findByUserId
    // and doesn't block anything.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.user.id = :userId")
    Optional<Cart> findByUserIdForUpdate(
        @Param("userId") Long userId
    );
}