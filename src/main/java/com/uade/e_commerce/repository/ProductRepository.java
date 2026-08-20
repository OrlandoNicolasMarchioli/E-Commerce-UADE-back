package com.uade.e_commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.Product;

/**
 * JpaRepository Provides CRUD operations and additional query methods for the Product
 * save, update, delete, findById, findAll, etc. de la tabla productos
 * ProductRepository
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

}
