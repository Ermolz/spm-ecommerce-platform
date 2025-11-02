package com.example.inventoryservice.api;

import com.example.inventoryservice.domain.InventoryItem;
import com.example.inventoryservice.service.InventoryService;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryAdminController {

    private final InventoryService service;

    @PutMapping("/stock/{productId}")
    public ResponseEntity<InventoryItem> upsertStock(
            @PathVariable Long productId,
            @RequestParam("qty") @PositiveOrZero int qty) {
        return ResponseEntity.ok(service.upsertStock(productId, qty));
    }
}