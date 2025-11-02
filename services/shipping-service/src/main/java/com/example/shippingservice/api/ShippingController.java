package com.example.shippingservice.api;

import com.example.shippingservice.domain.Shipment;
import com.example.shippingservice.repo.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShipmentRepository repo;

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<Optional<Shipment>> byOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(repo.findByOrderId(orderId));
    }
}