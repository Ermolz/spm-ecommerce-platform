package com.example.inventoryservice.jms;

import com.example.inventoryservice.config.MessagingProperties;
import com.example.inventoryservice.jms.messages.InventoryReserveCommand;
import com.example.inventoryservice.jms.messages.InventoryReserveReply;
import com.example.inventoryservice.service.InventoryService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryReserveListener {

    private final InventoryService inventoryService;
    private final MessagingProperties props;
    private final JmsTemplate queueJmsTemplate;

    @JmsListener(destination = "${app.jms.queue-inventory-reserve}")
    public void onReserve(InventoryReserveCommand cmd, Message inbound) throws JMSException {
        boolean ok = inventoryService.tryReserve(cmd.getProductId(), cmd.getQuantity());

        InventoryReserveReply reply = ok
                ? new InventoryReserveReply(true, null)
                : new InventoryReserveReply(false, "Insufficient stock");

        String correlationId = inbound.getJMSCorrelationID();

        queueJmsTemplate.convertAndSend(props.getQueueInventoryReserveReply(), reply, m -> {
            if (correlationId != null) m.setJMSCorrelationID(correlationId);
            m.setStringProperty("type", "INVENTORY_RESERVE_REPLY");
            return m;
        });
    }
}
