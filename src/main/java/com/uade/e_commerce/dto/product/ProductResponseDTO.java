package com.uade.e_commerce.dto.product;

import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.dto.category.CategoryResponseDTO;
import com.uade.e_commerce.model.ProductType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private ProductType type;
    private Integer stock;
    private Integer minutesDuration;
    private String attendanceType;
    private CategoryResponseDTO category;
    private Long publisherId;

    public static ProductResponseDTO fromEntity(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getType(),
                product.getStock(),
                product.getMinutesDuration(),
                product.getAttendanceType(),
                CategoryResponseDTO.fromEntity(product.getCategory()),
                product.getPublisher().getId()

                    // Devolvemos la categoría completa (anidada), no solo el id, para
                    // que el frontend no tenga que hacer un segundo pedido para
                    // mostrar el nombre de la categoría.
        );
    }
}
