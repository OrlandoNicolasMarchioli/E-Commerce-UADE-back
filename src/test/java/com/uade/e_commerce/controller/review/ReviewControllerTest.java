package com.uade.e_commerce.controller.review;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.uade.e_commerce.exception.DuplicateReviewException;
import com.uade.e_commerce.exception.ReviewNotFoundException;
import com.uade.e_commerce.model.Product;
import com.uade.e_commerce.model.Review;
import com.uade.e_commerce.model.User;
import com.uade.e_commerce.service.ReviewService;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    private Review buildReview() {
        Product product = new Product();
        product.setId(1L);
        User user = new User();
        user.setId(2L);

        Review review = new Review();
        review.setId(10L);
        review.setRating(5);
        review.setComment("Muy bueno");
        review.setProduct(product);
        review.setUser(user);
        return review;
    }

    @Test
    void getReviewsByProduct_returnsList() throws Exception {
        when(reviewService.getReviewsByProduct(1L)).thenReturn(List.of(buildReview()));

        mockMvc.perform(get("/api/products/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void getReviewById_found_returnsOk() throws Exception {
        when(reviewService.getReviewById(1L, 10L)).thenReturn(buildReview());

        mockMvc.perform(get("/api/products/1/reviews/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(2));
    }

    @Test
    void getReviewById_notFound_returns404() throws Exception {
        when(reviewService.getReviewById(1L, 99L)).thenThrow(new ReviewNotFoundException(99L));

        mockMvc.perform(get("/api/products/1/reviews/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReview_valid_returnsCreated() throws Exception {
        when(reviewService.createReview(eq(1L), eq(2L), any())).thenReturn(buildReview());

        mockMvc.perform(post("/api/products/1/reviews?userId=2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"comment\":\"Muy bueno\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(1));
    }

    @Test
    void createReview_duplicate_returns409() throws Exception {
        when(reviewService.createReview(eq(1L), eq(2L), any()))
                .thenThrow(new DuplicateReviewException(2L, 1L));

        mockMvc.perform(post("/api/products/1/reviews?userId=2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":5,\"comment\":\"Muy bueno\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void updateReview_found_returnsOk() throws Exception {
        when(reviewService.updateReview(eq(1L), eq(10L), any())).thenReturn(buildReview());

        mockMvc.perform(put("/api/products/1/reviews/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":3,\"comment\":\"Cambié de opinión\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateReview_notFound_returns404() throws Exception {
        when(reviewService.updateReview(eq(1L), eq(99L), any())).thenThrow(new ReviewNotFoundException(99L));

        mockMvc.perform(put("/api/products/1/reviews/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rating\":3,\"comment\":\"x\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReview_found_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/1/reviews/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReview_notFound_returns404() throws Exception {
        org.mockito.Mockito.doThrow(new ReviewNotFoundException(99L))
                .when(reviewService).deleteReview(1L, 99L);

        mockMvc.perform(delete("/api/products/1/reviews/99"))
                .andExpect(status().isNotFound());
    }
}
