package com.uade.e_commerce.controller.product;

import com.uade.e_commerce.dto.product.ProductImageRequestDTO;
import com.uade.e_commerce.dto.product.ProductImageResponseDTO;
import com.uade.e_commerce.service.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products/{productId}/images")
public class ProductImageController {

    @Autowired
    private ProductImageService productImageService;

    @GetMapping
    public ResponseEntity<?> getImagesByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(productImageService.getImagesByProductId(productId));
    }

    @PostMapping
    public ResponseEntity<?> addImageToProduct(
        @PathVariable Long productId, 
        @RequestBody ProductImageRequestDTO requestDTO) {
        ProductImageResponseDTO responseDTO = productImageService.addImageToProduct(productId, requestDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteProductImage(
        @PathVariable Long productId, 
        @PathVariable Long imageId) {
        productImageService.deleteProductImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }
}
