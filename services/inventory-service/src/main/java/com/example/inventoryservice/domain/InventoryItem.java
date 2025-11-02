package com.example.inventoryservice.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Product identifier (matches order-service productId)
    @Column(nullable = false, unique = true)
    private Long productId;

    // Available quantity in stock
    @Column(nullable = false)
    private int availableQty;
}
