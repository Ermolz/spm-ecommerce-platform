package com.example.inventoryservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "grpc.server.port=0",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.artemis.broker-url=vm://localhost?broker.persistent=false"
})
class InventoryServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
