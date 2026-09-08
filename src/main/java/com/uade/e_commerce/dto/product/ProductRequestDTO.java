package com.uade.e_commerce.dto.product;

import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.ProductType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


// What comes in the body of a POST/PUT to /api/products.
// It doesn't have "category" (the full object) but "categoryId" (just
// the number). It's the controller that looks up the real Category in the
// database from that id — the DTO shouldn't know anything about JPA.


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

        // category and publisher are left unset on purpose: the
        // controller fills them in after looking them up in the database.
        
        return product;
    }
}
