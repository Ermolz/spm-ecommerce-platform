package com.example.orderservice.jms.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReserveReply {
    private boolean reserved;
    private String reason;
}