package com.example.orderservice.grpc.exception;

public class GrpcInventoryException extends RuntimeException {
    public GrpcInventoryException(String message) {
        super(message);
    }

    public GrpcInventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}

