package com.uade.e_commerce.dto.product;

import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


// Lo que llega en el body de un POST/PUT a /api/productos.
// No tiene "category" (el objeto completo) sino "categoryId" (solo
// el número). Es el controller el que busca la Category real en la base
// a partir de ese id — el DTO no debería saber nada de JPA.


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    private String nombre;
    private String descripcion;
    private Double precio;
    private ProductType type;
    private Integer stock;
    private Integer duracionMinutos;
    private String modalidad;
    private Long categoryId;

    public Product toEntity() {
        Product product = new Product();
        product.setNombre(nombre);
        product.setDescripcion(descripcion);
        product.setPrecio(precio);
        product.setType(type);
        product.setStock(stock);
        product.setDuracionMinutos(duracionMinutos);
        product.setModalidad(modalidad);

        // category y publicador quedan sin setear a propósito: el
        // controller los completa después de buscarlos en la base.
        
        return product;
    }
}
