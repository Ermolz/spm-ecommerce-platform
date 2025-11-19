package com.example.orderservice.grpc.exception;

public class InventoryServiceUnavailableException extends GrpcInventoryException {
    public InventoryServiceUnavailableException(String message) {
        super(message);
    }

    public InventoryServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

