package com.uade.e_commerce.repository;

import com.uade.e_commerce.model.ProductImage;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderByImageOrderAsc(Long productId);
    
}
