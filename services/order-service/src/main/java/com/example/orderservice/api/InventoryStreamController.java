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
        final Object lock = new Object();
        configureEmitterCallbacks(emitter);

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
                                synchronized (lock) {
                                    handleStockUpdate(emitter, update);
                                }
                            }

                            @Override
                            public void onError(String error) {
                                synchronized (lock) {
                                    handleStreamError(emitter, error);
                                }
                            }

                            @Override
                            public void onCompleted() {
                                synchronized (lock) {
                                    handleStreamCompleted(emitter);
                                }
                            }
                        }
                );
            } catch (Exception e) {
                log.error("Error in stock stream", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private void configureEmitterCallbacks(SseEmitter emitter) {
        emitter.onCompletion(
                () -> log.debug("SSE emitter completed, client disconnected")
        );
        emitter.onTimeout(
                () -> log.debug("SSE emitter timeout")
        );
        emitter.onError(
                ex -> log.debug("SSE emitter error: {}", ex.getMessage())
        );
    }

    private void handleStockUpdate(SseEmitter emitter, StockUpdate update) {
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
        } catch (IllegalStateException e) {
            log.debug("SSE emitter already completed, stopping stream");
        } catch (IOException e) {
            log.debug("Client disconnected, stopping stream");
        } catch (Exception e) {
            log.error("Error sending SSE update", e);
        }
    }

    private void handleStreamError(SseEmitter emitter, String error) {
        sendErrorEventSafely(emitter, error);
        completeEmitterWithErrorSafely(emitter, error);
    }

    private void sendErrorEventSafely(SseEmitter emitter, String error) {
        try {
            emitter.send(SseEmitter.event()
                    .data(error)
                    .name("error"));
        } catch (Exception e) {
            log.debug("Could not send error event, emitter may be closed");
        }
    }

    private void completeEmitterWithErrorSafely(SseEmitter emitter, String error) {
        try {
            emitter.completeWithError(new RuntimeException(error));
        } catch (Exception e) {
            log.debug("Error completing emitter with error", e);
        }
    }

    private void handleStreamCompleted(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("Error completing emitter", e);
        }
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
