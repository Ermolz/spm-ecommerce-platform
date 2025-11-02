package com.example.orderservice.config;

import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig {

    // JSON <-> TextMessage
    @Bean
    public MappingJackson2MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter c = new MappingJackson2MessageConverter();
        c.setTargetType(MessageType.TEXT);
        c.setTypeIdPropertyName("_type");
        return c;
    }

    // Queue (point-to-point)
    @Bean
    public JmsTemplate queueJmsTemplate(ConnectionFactory cf,
                                        MappingJackson2MessageConverter converter) {
        JmsTemplate t = new JmsTemplate(cf);
        t.setMessageConverter(converter);
        t.setPubSubDomain(false);
        t.setReceiveTimeout(5000); // ms
        return t;
    }

    // Topic (pub/sub)
    @Bean
    public JmsTemplate topicJmsTemplate(ConnectionFactory cf,
                                        MappingJackson2MessageConverter converter) {
        JmsTemplate t = new JmsTemplate(cf);
        t.setMessageConverter(converter);
        t.setPubSubDomain(true);
        return t;
    }

    // Listener factory for queues (if needed later)
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory cf, MappingJackson2MessageConverter converter) {
        DefaultJmsListenerContainerFactory f = new DefaultJmsListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(converter);
        f.setPubSubDomain(false);
        return f;
    }

    // Listener factory for topics (if needed later)
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