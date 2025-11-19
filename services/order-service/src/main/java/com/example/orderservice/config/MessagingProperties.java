package com.example.orderservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class MessagingProperties {
    // JMS configuration
    private JmsConfig jms = new JmsConfig();
    // Use gRPC for inventory calls (true) or JMS (false)
    private boolean useGrpc = false;

    @Getter
    @Setter
    public static class JmsConfig {
        // P2P command queue for inventory reservation
        private String queueInventoryReserve;
        // P2P reply queue where inventory replies back
        private String queueInventoryReserveReply;
        // Pub/Sub topic for order events
        private String topicOrderEvents;
    }

    // Backward compatibility methods
    public String getQueueInventoryReserve() {
        return jms.getQueueInventoryReserve();
    }

    public String getQueueInventoryReserveReply() {
        return jms.getQueueInventoryReserveReply();
    }

    public String getTopicOrderEvents() {
        return jms.getTopicOrderEvents();
    }
}