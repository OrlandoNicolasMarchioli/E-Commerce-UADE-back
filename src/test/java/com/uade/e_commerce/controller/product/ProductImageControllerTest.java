package com.uade.e_commerce.controller.product;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.uade.e_commerce.dto.product.ProductImageResponseDTO;
import com.uade.e_commerce.service.ProductImageService;

@WebMvcTest(ProductImageController.class)
class ProductImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductImageService productImageService;

    private ProductImageResponseDTO buildResponse() {
        ProductImageResponseDTO dto = new ProductImageResponseDTO();
        dto.setId(1L);
        dto.setUrl("http://img/1.png");
        dto.setImageOrder(1);
        dto.setProductId(1L);
        return dto;
    }

    @Test
    void getImagesByProductId_returnsList() throws Exception {
        when(productImageService.getImagesByProductId(1L)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/products/1/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].url").value("http://img/1.png"));
    }

    @Test
    void addImageToProduct_returnsCreated() throws Exception {
        when(productImageService.addImageToProduct(eq(1L), any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/products/1/images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"http://img/1.png\",\"imageOrder\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteProductImage_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/1/images/1"))
                .andExpect(status().isNoContent());
    }
}
