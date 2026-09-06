package com.uade.e_commerce.dto.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lo que llega en el body de un POST/PUT a /api/products/{productId}/reviews.
 *
 * Solo trae los dos datos que el usuario realmente escribe. El producto,
 * el autor y la fecha NO vienen del body: el producto sale de la URL, el
 * autor del parámetro userId y la fecha la pone el servidor. Si dejáramos
 * que vinieran en el JSON, cualquiera podría mandar una reseña a nombre
 * de otro o falsear la fecha.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {

    private Integer rating;
    private String comment;
}
