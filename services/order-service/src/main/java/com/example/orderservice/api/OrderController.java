package com.example.orderservice.api;

import com.example.orderservice.domain.OrderEntity;
import com.example.orderservice.jms.messages.OrderCreateRequest;
import com.example.orderservice.repo.OrderRepository;
import com.example.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<OrderEntity> create(@Valid @RequestBody OrderCreateRequest req) {
        OrderEntity saved = orderService.createOrder(req);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<OrderEntity>> list(@RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderRepository.findAll(PageRequest.of(0, Math.max(1, Math.min(size, 100))))
                .getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<OrderEntity>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderRepository.findById(id));
    }
}