package com.uade.e_commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {


    // Used for the home page, which asks for the alphabetical listing.
    List<Product> findAllByOrderByNameAsc();

    List<Product> findByCategoryId(Long categoryId);
}