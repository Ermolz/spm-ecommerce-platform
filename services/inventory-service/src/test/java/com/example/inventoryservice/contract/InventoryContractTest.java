package com.example.inventoryservice.contract;

import com.example.inventoryservice.domain.InventoryItem;
import com.example.inventoryservice.repo.InventoryItemRepository;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.inventoryservice.jms.InventoryReserveListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = {
    "spring.profiles.active=test",
    "grpc.server.port=0",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.artemis.broker-url=vm://localhost?broker.persistent=false",
    "spring.jms.listener.auto-startup=false"
})
public abstract class InventoryContractTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private InventoryItemRepository repository;
    
    @MockBean
    private JmsTemplate queueJmsTemplate;
    
    @MockBean
    private DefaultJmsListenerContainerFactory jmsListenerContainerFactory;
    
    @MockBean
    private MappingJackson2MessageConverter jacksonJmsMessageConverter;
    
    @MockBean
    private InventoryReserveListener inventoryReserveListener;
    
    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public ConnectionFactory connectionFactory() throws JMSException {
            ConnectionFactory factory = mock(ConnectionFactory.class);
            Connection connection = mock(Connection.class);
            when(factory.createConnection()).thenReturn(connection);
            return factory;
        }
    }

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.webAppContextSetup(context);
        
        // Setup default mock behavior for product ID 1 (exists)
        when(repository.findByProductId(1L))
                .thenReturn(Optional.of(InventoryItem.builder()
                        .id(1L)
                        .productId(1L)
                        .availableQty(50)
                        .build()));
        
        // Setup default mock behavior for product ID 999 (does not exist)
        when(repository.findByProductId(999L))
                .thenReturn(Optional.empty());
    }
}

