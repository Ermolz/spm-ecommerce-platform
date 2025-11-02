package com.example.shippingservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.jms")
public class MessagingProperties {
    // Topic to consume order events from
    private String topicOrderEvents;
}