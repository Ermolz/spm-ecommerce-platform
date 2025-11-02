package com.example.orderservice.api;

import com.example.orderservice.domain.OrderEntity;
import com.example.orderservice.jms.messages.OrderCreateRequest;
import com.example.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderEntity> create(@Valid @RequestBody OrderCreateRequest req) {
        OrderEntity saved = orderService.createOrder(req);
        return ResponseEntity.ok(saved);
    }
}