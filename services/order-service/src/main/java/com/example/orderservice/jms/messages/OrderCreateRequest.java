package com.example.orderservice.jms.messages;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderCreateRequest {
    @NotNull
    private Long productId;

    @Positive
    private int quantity;

    // LOW / MEDIUM / HIGH — used by analytics selectors
    private String priority = "MEDIUM";
}
