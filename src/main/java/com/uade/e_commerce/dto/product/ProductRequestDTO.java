package com.uade.e_commerce.dto.product;

import com.uade.e_commerce.model.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    private String nombre;
    private String descripcion;
    private Double precio;

    public Product toEntity() {
        Product product = new Product();
        product.setNombre(nombre);
        product.setDescripcion(descripcion);
        product.setPrecio(precio);
        return product;
    }
}
