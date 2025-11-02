package com.example.shippingservice.jms;

import com.example.shippingservice.config.MessagingProperties;
import com.example.shippingservice.domain.Shipment;
import com.example.shippingservice.service.ShipmentService;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventsListener {

    private final ShipmentService service;
    private final MessagingProperties props;

    @JmsListener(
            destination = "${app.jms.topic-order-events}",
            containerFactory = "topicListenerContainerFactory"
    )
    public void onEvent(ShipOrderEventPayload payload, Message msg) {
        if ("ORDER_RESERVED".equalsIgnoreCase(payload.type())) {
            Shipment s = service.createForOrder(payload.orderId());
            log.info("Shipment created id={}, orderId={}, tracking={}",
                    s.getId(), s.getOrderId(), s.getTrackingNumber());
        } else {
            log.debug("Event ignored type={}, orderId={}", payload.type(), payload.orderId());
        }
    }

    public record ShipOrderEventPayload(Long orderId, String type, String priority) { }
}