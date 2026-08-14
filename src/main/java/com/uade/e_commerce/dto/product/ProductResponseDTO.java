package com.uade.e_commerce.dto.product;

import com.uade.e_commerce.model.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;

    public static ProductResponseDTO fromEntity(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getNombre(),
                product.getDescripcion(),
                product.getPrecio());
    }
}
