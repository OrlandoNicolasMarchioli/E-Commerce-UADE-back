package com.uade.e_commerce.dto.category;

import com.uade.e_commerce.model.Category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDTO {

    private String name;

    public Category toEntity() {
        Category category = new Category();
        category.setName(name);
        return category;
    }
}
