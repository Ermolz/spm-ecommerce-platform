package com.example.orderservice.client;

import com.example.orderservice.client.dto.StockResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryRestClient {

    private final RestTemplate restTemplate;

    @Value("${app.inventory.service.url:http://localhost:8082}")
    private String inventoryServiceUrl;

    public StockResponse getStock(Long productId) {
        try {
            String url = inventoryServiceUrl + "/api/inventory/stock/" + productId;
            ResponseEntity<StockResponse> response = restTemplate.getForEntity(url, StockResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Product {} not found in inventory", productId);
            return StockResponse.builder()
                    .productId(productId)
                    .availableQuantity(0)
                    .exists(false)
                    .build();
        } catch (RestClientException e) {
            log.error("Error calling inventory service for product {}", productId, e);
            throw new RuntimeException("Failed to get stock from inventory service", e);
        }
    }
}

