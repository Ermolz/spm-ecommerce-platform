package com.example.inventoryservice.config;

import com.example.inventoryservice.domain.InventoryItem;
import com.example.inventoryservice.repo.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final InventoryItemRepository repository;

    @Bean
    @Profile("!test")
    public CommandLineRunner initData() {
        return args -> {

            if (repository.count() > 0) {
                log.info("Inventory data already exists, skipping initialization");
                return;
            }

            log.info("Initializing inventory data...");

            createProduct(1L, 50, "Product 1 - Laptop");
            createProduct(2L, 30, "Product 2 - Smartphone");
            createProduct(3L, 100, "Product 3 - Headphones");
            createProduct(4L, 25, "Product 4 - Mouse");
            createProduct(5L, 75, "Product 5 - Keyboard");

            log.info("Inventory initialization completed. Created {} items", repository.count());
        };
    }

    private void createProduct(Long productId, int quantity, String description) {
        InventoryItem item = repository.findByProductId(productId)
                .orElseGet(() -> {
                    InventoryItem newItem = InventoryItem.builder()
                            .productId(productId)
                            .availableQty(quantity)
                            .build();
                    return repository.save(newItem);
                });

        if (item.getAvailableQty() != quantity) {
            item.setAvailableQty(quantity);
            repository.save(item);
        }

        log.info("Product {} ({}): {} units available", productId, description, quantity);
    }
}

