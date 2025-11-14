package com.example.orderservice.api;

import com.example.inventoryservice.grpc.StockUpdate;
import com.example.orderservice.grpc.InventoryGrpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/api/inventory/stream")
@RequiredArgsConstructor
public class InventoryStreamController {

    private final InventoryGrpcClient inventoryGrpcClient;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @GetMapping(value = "/{productId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStockUpdates(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "2") int intervalSeconds) {

        SseEmitter emitter = new SseEmitter(60000L);

        executorService.execute(() -> {
            try {
                if (inventoryGrpcClient == null) {
                    log.error("gRPC client is not available");
                    emitter.completeWithError(new IllegalStateException("gRPC client not available"));
                    return;
                }
                
                inventoryGrpcClient.streamStockUpdates(
                        productId,
                        intervalSeconds,
                        new InventoryGrpcClient.StockUpdateCallback() {
                            @Override
                            public void onUpdate(StockUpdate update) {
                                try {
                                    StockUpdateDTO dto = new StockUpdateDTO(
                                            update.getProductId(),
                                            update.getCurrentQuantity(),
                                            update.getTimestamp(),
                                            update.getStatus()
                                    );
                                    emitter.send(SseEmitter.event()
                                            .data(dto)
                                            .name("stock-update"));
                                } catch (IOException e) {
                                    log.error("Error sending SSE update", e);
                                    emitter.completeWithError(e);
                                }
                            }

                            @Override
                            public void onError(String error) {
                                try {
                                    emitter.send(SseEmitter.event()
                                            .data(error)
                                            .name("error"));
                                    emitter.completeWithError(new RuntimeException(error));
                                } catch (IOException e) {
                                    log.error("Error sending SSE error", e);
                                    emitter.completeWithError(e);
                                }
                            }

                            @Override
                            public void onCompleted() {
                                emitter.complete();
                            }
                        });
            } catch (Exception e) {
                log.error("Error in stock stream", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    public static class StockUpdateDTO {
        private Long productId;
        private Integer currentQuantity;
        private Long timestamp;
        private String status;

        public StockUpdateDTO(Long productId, Integer currentQuantity, Long timestamp, String status) {
            this.productId = productId;
            this.currentQuantity = currentQuantity;
            this.timestamp = timestamp;
            this.status = status;
        }

        public Long getProductId() {
            return productId;
        }

        public Integer getCurrentQuantity() {
            return currentQuantity;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public String getStatus() {
            return status;
        }
    }
}

