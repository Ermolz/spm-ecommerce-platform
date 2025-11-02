package com.example.orderservice.service;

import com.example.orderservice.config.MessagingProperties;
import com.example.orderservice.domain.OrderEntity;
import com.example.orderservice.domain.OrderStatus;
import com.example.orderservice.jms.messages.InventoryReserveCommand;
import com.example.orderservice.jms.messages.InventoryReserveReply;
import com.example.orderservice.jms.messages.OrderCreateRequest;
import com.example.orderservice.jms.messages.OrderEvent;
import com.example.orderservice.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MessagingProperties props;
    private final JmsTemplate queueJmsTemplate;
    private final JmsTemplate topicJmsTemplate;

    // Create order -> send P2P command -> wait for reply -> publish event
    @Transactional
    public OrderEntity createOrder(OrderCreateRequest req) {
        OrderEntity order = new OrderEntity();
        order.setProductId(req.getProductId());
        order.setQuantity(req.getQuantity());
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);

        String correlationId = UUID.randomUUID().toString();

        // Send command to inventory
        InventoryReserveCommand cmd =
                new InventoryReserveCommand(order.getId(), req.getProductId(), req.getQuantity());

        final String replyQueue = props.getQueueInventoryReserveReply();

        queueJmsTemplate.convertAndSend(props.getQueueInventoryReserve(), cmd, m -> {
            m.setStringProperty("type", "INVENTORY_RESERVE");
            m.setStringProperty("priority", normalizePriority(req.getPriority()));
            m.setJMSCorrelationID(correlationId);
            m.setStringProperty(JmsHeaders.REPLY_TO, replyQueue);
            return m;
        });

        InventoryReserveReply reply = (InventoryReserveReply) queueJmsTemplate.receiveSelectedAndConvert(
                replyQueue,
                "JMSCorrelationID = '" + correlationId + "'"
        );

        boolean reserved = false;
        String reason = null;

        if (reply != null) {
            reserved = reply.isReserved();
            reason = reply.getReason();
        }

        if (reserved) {
            order.setStatus(OrderStatus.RESERVED);
            publishEvent(order.getId(), "ORDER_RESERVED", req.getPriority());
        } else {
            order.setStatus(OrderStatus.REJECTED);
            publishEvent(order.getId(), "ORDER_REJECTED", req.getPriority());
        }

        return orderRepository.save(order);
    }

    private void publishEvent(Long orderId, String type, String priority) {
        OrderEvent event = new OrderEvent(orderId, type, normalizePriority(priority));
        topicJmsTemplate.convertAndSend(props.getTopicOrderEvents(), event, m -> {
            m.setStringProperty("type", type);
            m.setStringProperty("priority", normalizePriority(priority));
            return m;
        });
    }

    private String normalizePriority(String p) {
        if (p == null) return "MEDIUM";
        String u = p.toUpperCase();
        return switch (u) {
            case "LOW", "MEDIUM", "HIGH" -> u;
            default -> "MEDIUM";
        };
    }
}
