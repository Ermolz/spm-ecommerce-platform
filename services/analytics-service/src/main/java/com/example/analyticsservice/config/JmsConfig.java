package com.example.analyticsservice.config;

import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
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
                com.example.analyticsservice.jms.OrderEventsListener.AnalyticsOrderEventPayload.class);
        c.setTypeIdMappings(map);

        return c;
    }

    // Listener factory configured for TOPICS (pub/sub)
    @Bean
    public DefaultJmsListenerContainerFactory topicListenerContainerFactory(
            @Qualifier("jmsConnectionFactory") ConnectionFactory cf,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory f = new DefaultJmsListenerContainerFactory();
        f.setPubSubDomain(true);
        configurer.configure(f, cf);
        f.setMessageConverter(jacksonJmsMessageConverter());
        return f;
    }
}