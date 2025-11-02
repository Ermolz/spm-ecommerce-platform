package com.example.inventoryservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.jms")
public class MessagingProperties {
    // Queue from which we consume reservation commands
    private String queueInventoryReserve;
    // Queue where we send replies back to order-service
    private String queueInventoryReserveReply;
}
