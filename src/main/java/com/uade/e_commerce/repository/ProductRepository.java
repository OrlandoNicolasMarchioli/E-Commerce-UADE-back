package com.uade.e_commerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.e_commerce.model.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long> {


    // Used for the home page, which asks for the alphabetical listing.
    List<Product> findAllByOrderByNameAsc();

    List<Product> findByCategoryId(Long categoryId);

    // Reads the product blocking the row until the transaction ends
    // (SELECT ... FOR UPDATE). It's used by the checkout, where the stock is
    // read and then written: without the lock, two simultaneous purchases
    // could both read the same stock and each subtract from it, leaving the
    // final value wrong (or even negative).
    //
    // Only the checkout needs it; regular reads of the catalog keep using
    // findById and don't block anything.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}