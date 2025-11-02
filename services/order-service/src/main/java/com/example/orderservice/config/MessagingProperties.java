package com.example.orderservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.jms")
public class MessagingProperties {
    // P2P command queue for inventory reservation
    private String queueInventoryReserve;
    // P2P reply queue where inventory replies back
    private String queueInventoryReserveReply;
    // Pub/Sub topic for order events
    private String topicOrderEvents;
}