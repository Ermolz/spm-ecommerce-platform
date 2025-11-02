package com.example.analyticsservice.jms;

import com.example.analyticsservice.config.MessagingProperties;
import com.example.analyticsservice.domain.AnalyticsEvent;
import com.example.analyticsservice.service.AnalyticsService;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventsListener {

    private final AnalyticsService service;
    private final MessagingProperties props;

    // Consumes events from a TOPIC using a JMS selector from config
    @JmsListener(
            destination = "${app.jms.topic-order-events}",
            containerFactory = "topicListenerContainerFactory",
            selector = "${app.jms.selector}"
    )
    public void onEvent(AnalyticsOrderEventPayload payload, Message msg) {
        AnalyticsEvent saved = service.save(payload.orderId(), payload.type(), payload.priority());
        log.info("Analytics stored event id={}, orderId={}, type={}, priority={}",
                saved.getId(), saved.getOrderId(), saved.getType(), saved.getPriority());
    }

    public record AnalyticsOrderEventPayload(Long orderId, String type, String priority) { }
}