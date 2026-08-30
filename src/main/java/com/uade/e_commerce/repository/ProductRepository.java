package com.uade.e_commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {


    // La usamos para la home, que pide el listado alfabético.
    List<Product> findAllByOrderByNombreAsc();

    List<Product> findByCategoryId(Long categoryId);
}