package com.uade.e_commerce.controller.cart;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uade.e_commerce.dto.cart.CartItemResponseDTO;
import com.uade.e_commerce.dto.cart.CartResponseDTO;
import com.uade.e_commerce.exception.InsufficientStockException;
import com.uade.e_commerce.exception.InvalidQuantityException;
import com.uade.e_commerce.service.CartService;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    private CartResponseDTO buildCartResponse() {

        CartItemResponseDTO item =
            new CartItemResponseDTO(
                20L,
                "Cuaderno",
                2,
                new BigDecimal("1000.00"),
                new BigDecimal("2000.00")
            );

        return new CartResponseDTO(
            10L,
            1L,
            List.of(item),
            new BigDecimal("2000.00")
        );
    }

    @Test
    void getCart_returnsOk() throws Exception {

        when(cartService.getCart(1L))
            .thenReturn(buildCartResponse());

        mockMvc.perform(
            get("/api/cart?userId=1")
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.id").value(10)
            )
            .andExpect(
                jsonPath("$.userId").value(1)
            )
            .andExpect(
                jsonPath("$.items[0].productId")
                    .value(20)
            )
            .andExpect(
                jsonPath("$.items[0].quantity")
                    .value(2)
            )
            .andExpect(
                jsonPath("$.total")
                    .value(2000.0)
            );
    }

    @Test
    void addItem_valid_returnsCreated()
        throws Exception {

        when(
            cartService.addItem(eq(1L), any())
        ).thenReturn(buildCartResponse());

        mockMvc.perform(
            post("/api/cart/items?userId=1")
                .contentType(
                    MediaType.APPLICATION_JSON
                )
                .content(
                    """
                    {
                        "productId": 20,
                        "quantity": 2
                    }
                    """
                )
        )
            .andExpect(status().isCreated())
            .andExpect(
                jsonPath("$.items[0].productId")
                    .value(20)
            )
            .andExpect(
                jsonPath("$.items[0].quantity")
                    .value(2)
            );
    }

    @Test
    void addItem_insufficientStock_returns409()
        throws Exception {

        when(
            cartService.addItem(eq(1L), any())
        ).thenThrow(
            new InsufficientStockException(
                20L,
                6,
                5
            )
        );

        mockMvc.perform(
            post("/api/cart/items?userId=1")
                .contentType(
                    MediaType.APPLICATION_JSON
                )
                .content(
                    """
                    {
                        "productId": 20,
                        "quantity": 2
                    }
                    """
                )
        )
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath("$.status").value(409)
            );
    }

    @Test
    void addItem_invalidQuantity_returns400()
        throws Exception {

        when(
            cartService.addItem(eq(1L), any())
        ).thenThrow(
            new InvalidQuantityException(0)
        );

        mockMvc.perform(
            post("/api/cart/items?userId=1")
                .contentType(
                    MediaType.APPLICATION_JSON
                )
                .content(
                    """
                    {
                        "productId": 20,
                        "quantity": 0
                    }
                    """
                )
        )
            .andExpect(
                status().isBadRequest()
            )
            .andExpect(
                jsonPath("$.status").value(400)
            );
    }

    @Test
    void removeItem_returnsNoContent()
        throws Exception {

        mockMvc.perform(
            delete(
                "/api/cart/items/20?userId=1"
            )
        )
            .andExpect(
                status().isNoContent()
            );

        verify(cartService)
            .removeItem(1L, 20L);
    }

    @Test
    void clearCart_returnsNoContent()
        throws Exception {

        mockMvc.perform(
            delete("/api/cart?userId=1")
        )
            .andExpect(
                status().isNoContent()
            );

        verify(cartService)
            .clearCart(1L);
    }
}