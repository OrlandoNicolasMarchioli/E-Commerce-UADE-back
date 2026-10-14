
package com.uade.e_commerce.dto.product;

import java.math.BigDecimal;

import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// The request carries a category ID instead of a Category entity.
// ProductService resolves the relationship before persistence.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    private String name;
    private String description;
    private BigDecimal price;
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

        // Category and publisher are intentionally left unset.
        // Their existence must be verified against the database.
        return product;
    }
}
