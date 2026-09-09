package com.uade.e_commerce.controller.order;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uade.e_commerce.dto.order.OrderResponseDTO;
import com.uade.e_commerce.dto.order.OrderStatusRequestDTO;
import com.uade.e_commerce.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // POST http://localhost:8080/api/orders?userId=1
    //
    // Checkout: takes no body because the content of the order is already in
    // the user's cart. 201 CREATED because a new resource is being created.
    @PostMapping
    public ResponseEntity<OrderResponseDTO> checkout(
        @RequestParam Long userId
    ) {

        OrderResponseDTO order =
            orderService.checkout(userId);

        return new ResponseEntity<>(
            order,
            HttpStatus.CREATED
        );
    }

    // GET http://localhost:8080/api/orders?userId=1
    @GetMapping
    public List<OrderResponseDTO> getOrdersByUser(
        @RequestParam Long userId
    ) {

        return orderService.getOrdersByUser(userId);
    }

    // GET http://localhost:8080/api/orders/5
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
        @PathVariable Long id
    ) {

        return ResponseEntity.ok(
            orderService.getOrderById(id)
        );
    }

    // PUT http://localhost:8080/api/orders/5/status
    // Body: { "status": "PAID" }
    //
    // Cancelling doesn't get its own endpoint: it's one more state change,
    // and going through here keeps a single place where the transition rules
    // are applied.
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(
        @PathVariable Long id,
        @RequestBody OrderStatusRequestDTO dto
    ) {

        return ResponseEntity.ok(
            orderService.updateStatus(
                id,
                dto.getStatus()
            )
        );
    }
}
