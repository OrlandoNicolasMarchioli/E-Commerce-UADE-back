package com.uade.e_commerce.dto.category;

import com.uade.e_commerce.model.Category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {

    private Long id;
    private String nombre;

    public static CategoryResponseDTO fromEntity(Category category) {
        return new CategoryResponseDTO(category.getId(), category.getNombre());
    }
}
