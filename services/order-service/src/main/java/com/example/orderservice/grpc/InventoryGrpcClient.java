package com.example.orderservice.grpc;

import com.example.inventoryservice.grpc.*;
import com.example.orderservice.grpc.exception.GrpcInventoryException;
import com.example.orderservice.grpc.exception.InventoryServiceUnavailableException;
import com.example.orderservice.grpc.exception.ProductNotFoundException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryGrpcClient {

    @GrpcClient("inventory-service")
    private InventoryServiceGrpc.InventoryServiceBlockingStub blockingStub;

    @GrpcClient("inventory-service")
    private InventoryServiceGrpc.InventoryServiceStub asyncStub;
    
    @Value("${grpc.client.inventory-service.address:static://localhost:9091}")
    private String grpcAddress;
    
    @Value("${app.grpc.auth-token:demo-token}")
    private String authToken;
    
    private ManagedChannel fallbackChannel;
    private InventoryServiceGrpc.InventoryServiceBlockingStub fallbackBlockingStub;
    private InventoryServiceGrpc.InventoryServiceStub fallbackAsyncStub;
    
    @jakarta.annotation.PostConstruct
    public void init() {
        if ((blockingStub == null || asyncStub == null) && fallbackChannel == null) {
            createFallbackChannel();
        }
    }
    
    private synchronized void createFallbackChannel() {
        if (fallbackChannel != null) {
            return;
        }
        
        try {
            String address = grpcAddress.replace("static://", "").replace("dns:///", "");
            
            String host;
            int port;
            
            if (address.contains(":")) {
                String[] parts = address.split(":");
                host = parts[0];
                port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9091;
            } else {
                host = address.isEmpty() ? "localhost" : address;
                port = 9091;
            }
            
            fallbackChannel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .build();
            
            io.grpc.ClientInterceptor authInterceptor = new io.grpc.ClientInterceptor() {
                @Override
                public <R1, R2> io.grpc.ClientCall<R1, R2> interceptCall(
                        io.grpc.MethodDescriptor<R1, R2> method,
                        io.grpc.CallOptions callOptions,
                        io.grpc.Channel next) {
                    return new io.grpc.ForwardingClientCall.SimpleForwardingClientCall<R1, R2>(
                            next.newCall(method, callOptions)) {
                        @Override
                        public void start(io.grpc.ClientCall.Listener<R2> responseListener, 
                                         io.grpc.Metadata headers) {
                            io.grpc.Metadata.Key<String> authKey = io.grpc.Metadata.Key.of(
                                    "authorization", io.grpc.Metadata.ASCII_STRING_MARSHALLER);
                            headers.put(authKey, "Bearer " + authToken);
                            super.start(responseListener, headers);
                        }
                    };
                }
            };
            
            fallbackBlockingStub = InventoryServiceGrpc.newBlockingStub(fallbackChannel)
                    .withInterceptors(authInterceptor)
                    .withWaitForReady();
            fallbackAsyncStub = InventoryServiceGrpc.newStub(fallbackChannel)
                    .withInterceptors(authInterceptor);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse gRPC address: {}", grpcAddress, e);
        } catch (RuntimeException e) {
            log.error("Failed to create fallback gRPC channel", e);
        }
    }
    
    private InventoryServiceGrpc.InventoryServiceBlockingStub getBlockingStub() {
        if (blockingStub != null) {
            return blockingStub;
        }
        if (fallbackBlockingStub == null) {
            createFallbackChannel();
        }
        return fallbackBlockingStub;
    }
    
    private InventoryServiceGrpc.InventoryServiceStub getAsyncStub() {
        if (asyncStub != null) {
            return asyncStub;
        }
        if (fallbackAsyncStub == null) {
            createFallbackChannel();
        }
        return fallbackAsyncStub;
    }

    public ReserveInventoryResponse reserveInventory(long productId, int quantity, long orderId) {
        try {
            InventoryServiceGrpc.InventoryServiceBlockingStub stub = getBlockingStub();
            if (stub == null) {
                throw new IllegalStateException("gRPC client not initialized");
            }

            ReserveInventoryRequest request = ReserveInventoryRequest.newBuilder()
                    .setProductId(productId)
                    .setQuantity(quantity)
                    .setOrderId(orderId)
                    .build();

            return stub.reserveInventory(request);

        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            log.error("gRPC ReserveInventory error: code={}, description={}", 
                    status.getCode(), status.getDescription(), e);

            // Handle different error codes
            if (status.getCode() == Status.Code.INVALID_ARGUMENT) {
                throw new IllegalArgumentException("Invalid request: " + status.getDescription());
            } else if (status.getCode() == Status.Code.NOT_FOUND) {
                throw new ProductNotFoundException("Product not found: " + status.getDescription());
            } else if (status.getCode() == Status.Code.UNAVAILABLE) {
                throw new InventoryServiceUnavailableException("Inventory service unavailable: " + status.getDescription());
            } else {
                throw new GrpcInventoryException("gRPC error: " + status.getDescription(), e);
            }
        } catch (IllegalArgumentException | GrpcInventoryException | IllegalStateException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Unexpected error in ReserveInventory", e);
            throw new GrpcInventoryException("Failed to reserve inventory: " + e.getMessage(), e);
        }
    }

    public GetStockResponse getStock(long productId) {
        try {
            InventoryServiceGrpc.InventoryServiceBlockingStub stub = getBlockingStub();
            if (stub == null) {
                throw new IllegalStateException("gRPC client not initialized");
            }

            GetStockRequest request = GetStockRequest.newBuilder()
                    .setProductId(productId)
                    .build();

            return stub.getStock(request);

        } catch (StatusRuntimeException e) {
            Status status = e.getStatus();
            log.error("gRPC GetStock error: code={}, description={}", 
                    status.getCode(), status.getDescription(), e);

            if (status.getCode() == Status.Code.NOT_FOUND) {
                throw new ProductNotFoundException("Product not found: " + status.getDescription());
            } else if (status.getCode() == Status.Code.INVALID_ARGUMENT) {
                throw new IllegalArgumentException("Invalid request: " + status.getDescription());
            } else {
                throw new GrpcInventoryException("gRPC error: " + status.getDescription(), e);
            }
        } catch (IllegalArgumentException | GrpcInventoryException | IllegalStateException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Unexpected error in GetStock", e);
            throw new GrpcInventoryException("Failed to get stock: " + e.getMessage(), e);
        }
    }

    public void streamStockUpdates(long productId, int intervalSeconds, 
                                   StockUpdateCallback callback) {
        try {
            InventoryServiceGrpc.InventoryServiceStub stub = getAsyncStub();
            if (stub == null) {
                callback.onError("gRPC client not initialized");
                return;
            }

            StreamStockUpdatesRequest request = StreamStockUpdatesRequest.newBuilder()
                    .setProductId(productId)
                    .setUpdateIntervalSeconds(intervalSeconds)
                    .build();

            AtomicReference<Throwable> errorRef = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            stub.streamStockUpdates(request, new io.grpc.stub.StreamObserver<StockUpdate>() {
                @Override
                public void onNext(StockUpdate update) {
                    try {
                        callback.onUpdate(update);
                    } catch (IllegalStateException e) {
                        log.debug("Callback rejected update, client may have disconnected");
                        errorRef.set(e);
                    } catch (RuntimeException e) {
                        log.debug("Error in stock update callback", e);
                        errorRef.set(e);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    errorRef.set(throwable);
                    latch.countDown();
                    
                    if (throwable instanceof StatusRuntimeException statusRuntimeException) {
                        Status status = statusRuntimeException.getStatus();
                        callback.onError("gRPC error: " + status.getCode() + " - " + status.getDescription());
                    } else {
                        callback.onError("Stream error: " + throwable.getMessage());
                    }
                }

                @Override
                public void onCompleted() {
                    callback.onCompleted();
                    latch.countDown();
                }
            });

            boolean completed = latch.await(60, TimeUnit.SECONDS);
            if (!completed) {
                callback.onError("Stream timeout");
            }

            Throwable error = errorRef.get();
            if (error != null && !(error instanceof StatusRuntimeException)) {
                throw new GrpcInventoryException("Stream processing error", error);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            callback.onError("Stream interrupted");
        } catch (GrpcInventoryException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Error in StreamStockUpdates", e);
            callback.onError("Failed to stream updates: " + e.getMessage());
        }
    }

    @FunctionalInterface
    public interface StockUpdateCallback {
        void onUpdate(StockUpdate update);
        
        default void onError(String error) {
            log.error("Stock update error: {}", error);
        }
        
        default void onCompleted() {
        }
    }
}

