package com.example.orderservice.service;

import com.example.inventoryservice.grpc.ReserveInventoryResponse;
import com.example.orderservice.config.MessagingProperties;
import com.example.orderservice.domain.OrderEntity;
import com.example.orderservice.domain.OrderStatus;
import com.example.orderservice.grpc.InventoryGrpcClient;
import com.example.orderservice.jms.messages.InventoryReserveCommand;
import com.example.orderservice.jms.messages.InventoryReserveReply;
import com.example.orderservice.jms.messages.OrderCreateRequest;
import com.example.orderservice.jms.messages.OrderEvent;
import com.example.orderservice.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.JmsHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MessagingProperties props;
    private final JmsTemplate queueJmsTemplate;
    private final JmsTemplate topicJmsTemplate;
    private final InventoryGrpcClient inventoryGrpcClient;

    // Create order -> reserve inventory (via gRPC or JMS) -> publish event
    @Transactional
    public OrderEntity createOrder(OrderCreateRequest req) {
        OrderEntity order = new OrderEntity();
        order.setProductId(req.getProductId());
        order.setQuantity(req.getQuantity());
        order.setStatus(OrderStatus.PENDING);
        order.setPriority(normalizePriority(req.getPriority()));
        order = orderRepository.save(order);

        boolean reserved = false;
        String reason = null;

        // Use gRPC or JMS based on configuration
        if (props.isUseGrpc()) {
            log.info("Using gRPC to reserve inventory for orderId={}, productId={}, quantity={}", 
                    order.getId(), req.getProductId(), req.getQuantity());
            
            try {
                ReserveInventoryResponse response = inventoryGrpcClient.reserveInventory(
                        req.getProductId(), 
                        req.getQuantity(), 
                        order.getId()
                );
                reserved = response.getReserved();
                reason = response.getReason();
            } catch (Exception e) {
                log.error("gRPC reservation failed for orderId={}", order.getId(), e);
                reserved = false;
                reason = "gRPC error: " + e.getMessage();
            }
        } else {
            log.info("Using JMS to reserve inventory for orderId={}, productId={}, quantity={}", 
                    order.getId(), req.getProductId(), req.getQuantity());
            
            String correlationId = UUID.randomUUID().toString();
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

            if (reply != null) {
                reserved = reply.isReserved();
                reason = reply.getReason();
            }
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
