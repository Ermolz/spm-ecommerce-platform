package com.example.orderservice.grpc.security;

import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientSecurityConfig {

    @GrpcGlobalClientInterceptor
    public GrpcClientSecurityInterceptor grpcClientSecurityInterceptor() {
        return new GrpcClientSecurityInterceptor();
    }
}

