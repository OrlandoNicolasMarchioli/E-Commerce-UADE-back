package com.uade.e_commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Items are always read in the same order so the response of an order
    // doesn't change between calls.
    List<OrderItem> findByOrderIdOrderByIdAsc(Long orderId);
}
