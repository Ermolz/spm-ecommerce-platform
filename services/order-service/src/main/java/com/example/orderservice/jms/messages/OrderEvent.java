package com.example.orderservice.jms.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private Long orderId;
    private String type;     // ORDER_CREATED / ORDER_RESERVED / ORDER_REJECTED
    private String priority; // LOW / MEDIUM / HIGH
}