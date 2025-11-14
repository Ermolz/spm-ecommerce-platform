package com.example.inventoryservice.config;

import com.example.inventoryservice.grpc.InventoryGrpcService;
import com.example.inventoryservice.grpc.security.GrpcSecurityInterceptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.io.IOException;

@Slf4j
@Configuration
public class GrpcServerConfig {

    @Value("${grpc.server.port:9091}")
    private int grpcPort;

    private final InventoryGrpcService inventoryGrpcService;
    private final GrpcSecurityInterceptor securityInterceptor;
    private Server grpcServer;

    public GrpcServerConfig(InventoryGrpcService inventoryGrpcService,
                           GrpcSecurityInterceptor securityInterceptor) {
        this.inventoryGrpcService = inventoryGrpcService;
        this.securityInterceptor = securityInterceptor;
    }

    @Bean
    @ConditionalOnMissingBean(name = "grpcServer")
    public Server grpcServer() throws IOException {
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(grpcPort);
        serverBuilder.intercept(securityInterceptor);
        serverBuilder.addService(inventoryGrpcService);
        
        this.grpcServer = serverBuilder.build();
        this.grpcServer.start();
        log.info("gRPC server started on port: {}", grpcPort);
        
        return this.grpcServer;
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (grpcServer != null && !grpcServer.isShutdown()) {
            log.info("Shutting down gRPC server...");
            grpcServer.shutdown();
            try {
                grpcServer.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("gRPC server shutdown interrupted", e);
            }
            log.info("gRPC server shut down");
        }
    }
}

