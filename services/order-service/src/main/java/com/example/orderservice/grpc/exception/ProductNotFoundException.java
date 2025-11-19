package com.example.orderservice.grpc.exception;

public class ProductNotFoundException extends GrpcInventoryException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

