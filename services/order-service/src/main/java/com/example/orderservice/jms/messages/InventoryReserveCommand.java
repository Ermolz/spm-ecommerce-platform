package com.example.orderservice.jms.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReserveCommand {
    private Long orderId;
    private Long productId;
    private int quantity;
}
