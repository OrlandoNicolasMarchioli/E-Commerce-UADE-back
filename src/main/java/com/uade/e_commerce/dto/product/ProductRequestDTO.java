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

    private String name;
    private String description;
    private Double price;
    private ProductType type;
    private Integer stock;
    private Integer minutesDuration;
    private String attendanceType;
    private Long categoryId;

    public Product toEntity() {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setType(type);
        product.setStock(stock);
        product.setMinutesDuration(minutesDuration);
        product.setAttendanceType(attendanceType);

        // category y publisher quedan sin setear a propósito: el
        // controller los completa después de buscarlos en la base.
        
        return product;
    }
}
