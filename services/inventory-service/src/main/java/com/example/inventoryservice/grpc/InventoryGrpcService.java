package com.example.inventoryservice.grpc;

import com.example.inventoryservice.domain.InventoryItem;
import com.example.inventoryservice.repo.InventoryItemRepository;
import com.example.inventoryservice.service.InventoryService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class InventoryGrpcService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final InventoryService inventoryService;
    private final InventoryItemRepository repository;

    private static final String INTERNAL_ERROR = "Internal server error: ";


    @Override
    @Transactional
    public void reserveInventory(ReserveInventoryRequest request, 
                                StreamObserver<ReserveInventoryResponse> responseObserver) {
        try {
            if (request.getProductId() <= 0 || request.getQuantity() <= 0) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Product ID and quantity must be positive")
                        .asRuntimeException());
                return;
            }

            Optional<InventoryItem> itemOpt = repository.findByProductId(request.getProductId());
            int availableQty = itemOpt.map(InventoryItem::getAvailableQty).orElse(0);

            boolean reserved = inventoryService.tryReserve(
                    request.getProductId(), 
                    request.getQuantity()
            );

            ReserveInventoryResponse response = ReserveInventoryResponse.newBuilder()
                    .setReserved(reserved)
                    .setReason(reserved ? "Reservation successful" : "Insufficient stock")
                    .setAvailableQuantity(availableQty)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in ReserveInventory", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(INTERNAL_ERROR + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }



    @Override
    public void getStock(GetStockRequest request, 
                        StreamObserver<GetStockResponse> responseObserver) {
        try {
            if (request.getProductId() <= 0) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Product ID must be positive")
                        .asRuntimeException());
                return;
            }

            Optional<InventoryItem> itemOpt = repository.findByProductId(request.getProductId());

            if (itemOpt.isEmpty()) {
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Product not found: " + request.getProductId())
                        .asRuntimeException());
                return;
            }

            InventoryItem item = itemOpt.get();
            GetStockResponse response = GetStockResponse.newBuilder()
                    .setProductId(item.getProductId())
                    .setAvailableQuantity(item.getAvailableQty())
                    .setExists(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in GetStock", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(INTERNAL_ERROR + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void streamStockUpdates(StreamStockUpdatesRequest request, 
                                   StreamObserver<StockUpdate> responseObserver) {
        try {
            if (request.getProductId() <= 0) {
                responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Product ID must be positive")
                        .asRuntimeException());
                return;
            }

            int intervalSeconds = request.getUpdateIntervalSeconds() > 0 
                    ? request.getUpdateIntervalSeconds() 
                    : 2;

            int maxUpdates = 30 / intervalSeconds;
            
            for (int i = 0; i < maxUpdates; i++) {
                if (responseObserver instanceof io.grpc.stub.ServerCallStreamObserver) {
                    io.grpc.stub.ServerCallStreamObserver<StockUpdate> serverObserver =
                            (io.grpc.stub.ServerCallStreamObserver<StockUpdate>) responseObserver;
                    if (serverObserver.isCancelled()) {
                        return;
                    }
                }

                Optional<InventoryItem> itemOpt = repository.findByProductId(request.getProductId());
                int currentQty = itemOpt.map(InventoryItem::getAvailableQty).orElse(0);
                
                String status;
                if (currentQty == 0) {
                    status = "OUT_OF_STOCK";
                } else if (currentQty < 10) {
                    status = "LOW_STOCK";
                } else {
                    status = "AVAILABLE";
                }

                StockUpdate update = StockUpdate.newBuilder()
                        .setProductId(request.getProductId())
                        .setCurrentQuantity(currentQty)
                        .setTimestamp(System.currentTimeMillis())
                        .setStatus(status)
                        .build();

                responseObserver.onNext(update);


                if (!sleepWithInterruptHandling(intervalSeconds)) {
                    break;
                }
            }

            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error in StreamStockUpdates", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(INTERNAL_ERROR + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    private boolean sleepWithInterruptHandling(long intervalSeconds) {
        try {
            TimeUnit.SECONDS.sleep(intervalSeconds);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

}

