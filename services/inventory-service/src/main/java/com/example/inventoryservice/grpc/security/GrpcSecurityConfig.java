package com.example.inventoryservice.grpc.security;

import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcSecurityConfig {

    @GrpcGlobalServerInterceptor
    public GrpcSecurityInterceptor grpcSecurityInterceptor() {
        return new GrpcSecurityInterceptor();
    }
}

