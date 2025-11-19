package com.example.inventoryservice.api;

import com.example.inventoryservice.api.dto.StockResponse;
import com.example.inventoryservice.repo.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryItemRepository repository;

    @GetMapping("/stock/{productId}")
    public ResponseEntity<StockResponse> getStock(@PathVariable Long productId) {
        return repository.findByProductId(productId)
                .map(item -> ResponseEntity.ok(StockResponse.builder()
                        .productId(item.getProductId())
                        .availableQuantity(item.getAvailableQty())
                        .exists(true)
                        .build()))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(StockResponse.builder()
                                .productId(productId)
                                .availableQuantity(0)
                                .exists(false)
                                .build()));
    }
}

