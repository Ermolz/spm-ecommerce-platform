package com.example.orderservice.contract;

import com.example.orderservice.client.InventoryRestClient;
import com.example.orderservice.client.dto.StockResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = {com.example.orderservice.client.InventoryRestClient.class, com.example.orderservice.config.RestConfig.class},
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
        "org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration"
    }
)
@AutoConfigureStubRunner(
    ids = "com.example:inventory-service:0.0.1-SNAPSHOT:stubs",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false",
    "grpc.client.inventory-service.address=static://localhost:0"
})
class InventoryClientContractTest {

    @Autowired
    private InventoryRestClient inventoryRestClient;

    @Autowired
    private StubFinder stubFinder;

    @BeforeEach
    void setup() {
        // Set the inventory service URL to point to the stub runner
        URL stubUrl = stubFinder.findStubUrl("com.example", "inventory-service");
        String stubBaseUrl = "http://localhost:" + stubUrl.getPort();
        ReflectionTestUtils.setField(inventoryRestClient, "inventoryServiceUrl", stubBaseUrl);
    }

    @Test
    void shouldGetStockWhenProductExists() {
        // Given: Product ID 1 exists (as defined in contract)
        Long productId = 1L;

        // When: Getting stock
        StockResponse response = inventoryRestClient.getStock(productId);

        // Then: Should return stock information
        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getAvailableQuantity()).isEqualTo(50);
        assertThat(response.getExists()).isTrue();
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() {
        // Given: Product ID 999 does not exist (as defined in contract)
        Long productId = 999L;

        // When: Getting stock
        StockResponse response = inventoryRestClient.getStock(productId);

        // Then: Should return not found response
        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(999L);
        assertThat(response.getAvailableQuantity()).isEqualTo(0);
        assertThat(response.getExists()).isFalse();
    }
}

