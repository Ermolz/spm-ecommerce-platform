package com.example.shippingservice.config;

import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
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

        Map<String, Class<?>> map = new HashMap<>();
        map.put("com.example.orderservice.jms.messages.OrderEvent",
                com.example.shippingservice.jms.OrderEventsListener.ShipOrderEventPayload.class);
        c.setTypeIdMappings(map);

        return c;
    }

    // Listener factory configured for TOPICS (pub/sub)
    @Bean
    public DefaultJmsListenerContainerFactory topicListenerContainerFactory(
            ConnectionFactory cf, MappingJackson2MessageConverter converter) {
        DefaultJmsListenerContainerFactory f = new DefaultJmsListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(converter);
        f.setPubSubDomain(true);
        return f;
    }
}