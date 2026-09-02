package com.uade.e_commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uade.e_commerce.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}