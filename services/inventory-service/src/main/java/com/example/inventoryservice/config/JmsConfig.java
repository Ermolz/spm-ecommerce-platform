package com.example.inventoryservice.config;

import com.example.inventoryservice.jms.messages.InventoryReserveCommand;
import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JmsConfig {

    @Bean
    public MappingJackson2MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter c = new MappingJackson2MessageConverter();
        c.setTargetType(MessageType.TEXT);
        c.setTypeIdPropertyName("_type");
        
        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put(
                "com.example.orderservice.jms.messages.InventoryReserveCommand",
                InventoryReserveCommand.class
        );
        c.setTypeIdMappings(typeIdMappings);
        
        return c;
    }

    // Queue template (point-to-point) used to send replies
    @Bean
    public JmsTemplate queueJmsTemplate(ConnectionFactory cf,
                                        MappingJackson2MessageConverter converter) {
        JmsTemplate t = new JmsTemplate(cf);
        t.setMessageConverter(converter);
        t.setPubSubDomain(false);
        return t;
    }

    // Listener factory for queues
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory cf, MappingJackson2MessageConverter converter) {
        DefaultJmsListenerContainerFactory f = new DefaultJmsListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(converter);
        f.setPubSubDomain(false);
        return f;
    }
}
