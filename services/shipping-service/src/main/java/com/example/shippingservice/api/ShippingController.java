package com.example.shippingservice.api;

import com.example.shippingservice.domain.Shipment;
import com.example.shippingservice.repo.ShipmentRepository;
import com.example.shippingservice.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShipmentRepository repo;
    private final ShipmentService shipmentService;

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<Optional<Shipment>> byOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(repo.findByOrderId(orderId));
    }

    @GetMapping
    public ResponseEntity<List<Shipment>> list(@RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(repo.findAll(PageRequest.of(0, Math.max(1, Math.min(size, 100))))
                .getContent());
    }

    @PostMapping("/create-for-order/{orderId}")
    public ResponseEntity<Shipment> createManually(@PathVariable Long orderId) {

        Shipment shipment = shipmentService.createForOrder(orderId);
        return ResponseEntity.ok(shipment);
    }
}