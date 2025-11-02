package com.example.shippingservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    // Simple generated tracking number
    @Column(nullable = false, unique = true)
    private String trackingNumber;

    // Shipment status
    @Column(nullable = false)
    private String status; // CREATED / DISPATCHED / DELIVERED

    private OffsetDateTime createdAt = OffsetDateTime.now();

    @PrePersist
    void prePersist() {
        if (trackingNumber == null || trackingNumber.isBlank()) {
            trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (status == null || status.isBlank()) status = "CREATED";
    }
}
