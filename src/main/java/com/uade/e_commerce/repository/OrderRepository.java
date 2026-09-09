package com.uade.e_commerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.e_commerce.model.Order;

import jakarta.persistence.LockModeType;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // The items (and the product of each item) are brought in the same query
    // as the order. Without this JOIN FETCH, building the response of a
    // history of N orders would run 1 query for the list plus 1 more per
    // order to read its items: the N+1 problem.
    //
    // DISTINCT is needed because joining a collection repeats the order once
    // per item it has.
    @Query(
        "select distinct o from Order o " +
        "left join fetch o.items i " +
        "left join fetch i.product " +
        "where o.user.id = :userId " +
        "order by o.orderDate desc"
    )
    List<Order> findByUserIdWithItems(
        @Param("userId") Long userId
    );

    // Same idea for a single order: one query instead of two.
    @Query(
        "select distinct o from Order o " +
        "left join fetch o.items i " +
        "left join fetch i.product " +
        "where o.id = :orderId"
    )
    Optional<Order> findByIdWithItems(
        @Param("orderId") Long orderId
    );

    // Reads the order blocking its row until the transaction ends. It's used
    // when changing the status: two simultaneous changes over the same order
    // (a double click on cancel, for instance) would both read the previous
    // status, both consider the transition valid, and both give the stock
    // back. With the lock the second one waits and, by then, it already sees
    // the new status.
    //
    // It doesn't bring the items: locking and fetching a collection in the
    // same query isn't supported the same way by every database. The items
    // are loaded afterwards, already inside the lock.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :orderId")
    Optional<Order> findByIdForUpdate(
        @Param("orderId") Long orderId
    );

    // The purchase history is shown newest first, which is what a user
    // expects when opening "my orders".
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
}
