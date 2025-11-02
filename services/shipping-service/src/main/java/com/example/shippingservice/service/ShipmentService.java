package com.example.shippingservice.service;

import com.example.shippingservice.domain.Shipment;
import com.example.shippingservice.repo.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository repo;

    @Transactional
    public Shipment createForOrder(Long orderId) {
        return repo.findByOrderId(orderId).orElseGet(() ->
                repo.save(Shipment.builder()
                        .orderId(orderId)
                        .status("CREATED")
                        .build()));
    }
}