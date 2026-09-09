package com.uade.e_commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // The purchase history is shown newest first, which is what a user
    // expects when opening "my orders".
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
}
