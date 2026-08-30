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
    private String nombre;
    private String descripcion;
    private Double precio;
    private ProductType type;
    private Integer stock;
    private Integer duracionMinutos;
    private String modalidad;
    private CategoryResponseDTO category;
    private Long publicadorId;

    public static ProductResponseDTO fromEntity(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getNombre(),
                product.getDescripcion(),
                product.getPrecio(),
                product.getType(),
                product.getStock(),
                product.getDuracionMinutos(),
                product.getModalidad(),
                CategoryResponseDTO.fromEntity(product.getCategory()),
                product.getPublicador().getId()

                    // Devolvemos la categoría completa (anidada), no solo el id, para
                    // que el frontend no tenga que hacer un segundo pedido para
                    // mostrar el nombre de la categoría.
        );
    }
}
