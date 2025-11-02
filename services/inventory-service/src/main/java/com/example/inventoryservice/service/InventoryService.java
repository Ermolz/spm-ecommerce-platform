package com.example.inventoryservice.service;

import com.example.inventoryservice.domain.InventoryItem;
import com.example.inventoryservice.repo.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryItemRepository repo;

    // Try to reserve quantity for a product; returns true if succeeded
    @Transactional
    public boolean tryReserve(Long productId, int qty) {
        InventoryItem item = repo.findByProductId(productId)
                .orElseGet(() -> repo.save(
                        InventoryItem.builder()
                                .productId(productId)
                                .availableQty(0)
                                .build()));

        if (qty <= 0) return false;
        if (item.getAvailableQty() < qty) return false;

        item.setAvailableQty(item.getAvailableQty() - qty);
        repo.save(item);
        return true;
    }

    // Admin helper to upsert stock
    @Transactional
    public InventoryItem upsertStock(Long productId, int newQty) {
        InventoryItem item = repo.findByProductId(productId)
                .orElseGet(() -> new InventoryItem(null, productId, 0));
        item.setAvailableQty(newQty);
        return repo.save(item);
    }
}