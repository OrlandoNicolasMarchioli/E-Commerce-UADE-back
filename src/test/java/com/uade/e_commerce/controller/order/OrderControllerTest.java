package com.uade.e_commerce.controller.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.uade.e_commerce.dto.order.OrderItemResponseDTO;
import com.uade.e_commerce.dto.order.OrderResponseDTO;
import com.uade.e_commerce.exception.EmptyCartException;
import com.uade.e_commerce.exception.InsufficientStockException;
import com.uade.e_commerce.exception.InvalidOrderStateException;
import com.uade.e_commerce.exception.OrderAccessDeniedException;
import com.uade.e_commerce.exception.OrderNotFoundException;
import com.uade.e_commerce.service.OrderService;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private OrderResponseDTO buildOrderResponse(
        String status
    ) {

        OrderItemResponseDTO item =
            new OrderItemResponseDTO(
                20L,
                "Cuaderno",
                2,
                new BigDecimal("1000.00"),
                new BigDecimal("2000.00")
            );

        return new OrderResponseDTO(
            100L,
            1L,
            LocalDateTime.now(),
            status,
            List.of(item),
            new BigDecimal("2000.00")
        );
    }

    @Test
    void checkout_returnsCreated() throws Exception {

        when(orderService.checkout(1L))
            .thenReturn(
                buildOrderResponse("PENDING")
            );

        mockMvc.perform(
            post("/api/orders?userId=1")
        )
            .andExpect(status().isCreated())
            .andExpect(
                jsonPath("$.id").value(100)
            )
            .andExpect(
                jsonPath("$.userId").value(1)
            )
            .andExpect(
                jsonPath("$.status")
                    .value("PENDING")
            )
            .andExpect(
                jsonPath("$.items[0].productName")
                    .value("Cuaderno")
            )
            .andExpect(
                jsonPath("$.items[0].unitPrice")
                    .value(1000.0)
            )
            .andExpect(
                jsonPath("$.total").value(2000.0)
            );
    }

    @Test
    void checkout_emptyCart_returns400()
        throws Exception {

        when(orderService.checkout(1L))
            .thenThrow(new EmptyCartException(1L));

        mockMvc.perform(
            post("/api/orders?userId=1")
        )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.status").value(400)
            );
    }

    @Test
    void checkout_insufficientStock_returns409()
        throws Exception {

        when(orderService.checkout(1L))
            .thenThrow(
                new InsufficientStockException(
                    20L,
                    6,
                    5
                )
            );

        mockMvc.perform(
            post("/api/orders?userId=1")
        )
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath("$.status").value(409)
            );
    }

    @Test
    void getOrdersByUser_returnsOk()
        throws Exception {

        when(orderService.getOrdersByUser(1L))
            .thenReturn(
                List.of(
                    buildOrderResponse("PENDING")
                )
            );

        mockMvc.perform(
            get("/api/orders?userId=1")
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$[0].id").value(100)
            )
            .andExpect(
                jsonPath("$[0].total").value(2000.0)
            );
    }

    @Test
    void getOrderById_returnsOk() throws Exception {

        when(orderService.getOrderById(100L, 1L))
            .thenReturn(
                buildOrderResponse("PAID")
            );

        mockMvc.perform(get("/api/orders/100?userId=1"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.status").value("PAID")
            )
            .andExpect(
                jsonPath("$.orderDate").exists()
            );
    }

    @Test
    void getOrderById_notFound_returns404()
        throws Exception {

        when(orderService.getOrderById(404L, 1L))
            .thenThrow(
                new OrderNotFoundException(404L)
            );

        mockMvc.perform(get("/api/orders/404?userId=1"))
            .andExpect(status().isNotFound())
            .andExpect(
                jsonPath("$.status").value(404)
            );
    }

    // Knowing the id isn't enough: the order has to belong to the user
    // asking for it.
    @Test
    void getOrderById_otherUsersOrder_returns403()
        throws Exception {

        when(orderService.getOrderById(100L, 2L))
            .thenThrow(
                new OrderAccessDeniedException(
                    100L,
                    2L
                )
            );

        mockMvc.perform(
            get("/api/orders/100?userId=2")
        )
            .andExpect(status().isForbidden())
            .andExpect(
                jsonPath("$.status").value(403)
            );
    }

    @Test
    void updateStatus_otherUsersOrder_returns403()
        throws Exception {

        when(
            orderService.updateStatus(
                eq(100L),
                eq(2L),
                eq("CANCELLED")
            )
        ).thenThrow(
            new OrderAccessDeniedException(100L, 2L)
        );

        mockMvc.perform(
            put("/api/orders/100/status?userId=2")
                .contentType(
                    MediaType.APPLICATION_JSON
                )
                .content(
                    """
                    {
                        "status": "CANCELLED"
                    }
                    """
                )
        )
            .andExpect(status().isForbidden())
            .andExpect(
                jsonPath("$.status").value(403)
            );
    }

    @Test
    void updateStatus_returnsOk() throws Exception {

        when(
            orderService.updateStatus(
                eq(100L),
                eq(1L),
                eq("PAID")
            )
        ).thenReturn(
            buildOrderResponse("PAID")
        );

        mockMvc.perform(
            put("/api/orders/100/status?userId=1")
                .contentType(
                    MediaType.APPLICATION_JSON
                )
                .content(
                    """
                    {
                        "status": "PAID"
                    }
                    """
                )
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.status").value("PAID")
            );

        verify(orderService)
            .updateStatus(100L, 1L, "PAID");
    }

    @Test
    void updateStatus_invalidTransition_returns400()
        throws Exception {

        when(
            orderService.updateStatus(
                eq(100L),
                eq(1L),
                eq("SHIPPED")
            )
        ).thenThrow(
            new InvalidOrderStateException(
                "PENDING",
                "cambiar a SHIPPED"
            )
        );

        mockMvc.perform(
            put("/api/orders/100/status?userId=1")
                .contentType(
                    MediaType.APPLICATION_JSON
                )
                .content(
                    """
                    {
                        "status": "SHIPPED"
                    }
                    """
                )
        )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.status").value(400)
            );
    }

    // An unknown value reaches the service as plain text and comes back as a
    // 400, instead of failing during deserialization and turning into a 500.
    @Test
    void updateStatus_unknownValue_returns400()
        throws Exception {

        when(
            orderService.updateStatus(
                eq(100L),
                eq(1L),
                eq("REGALADO")
            )
        ).thenThrow(
            new InvalidOrderStateException(
                "Estado de pedido inválido: REGALADO"
            )
        );

        mockMvc.perform(
            put("/api/orders/100/status?userId=1")
                .contentType(
                    MediaType.APPLICATION_JSON
                )
                .content(
                    """
                    {
                        "status": "REGALADO"
                    }
                    """
                )
        )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.status").value(400)
            );
    }
}
