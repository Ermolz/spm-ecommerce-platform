package com.example.inventoryservice.grpc.security;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GrpcSecurityInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> AUTHORIZATION_KEY =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        
        String authHeader = headers.get(AUTHORIZATION_KEY);

        boolean isAuthenticated = false;
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (isValidToken(token)) {
                isAuthenticated = true;
            }
        }

        if (!isAuthenticated) {
            log.warn("Unauthenticated gRPC request - rejecting");
            call.close(Status.UNAUTHENTICATED.withDescription("Missing or invalid token"), headers);
            return new ServerCall.Listener<ReqT>() {};
        }

        return next.startCall(call, headers);
    }

    private boolean isValidToken(String token) {
        return token != null && (token.equals("demo-token") || token.length() > 10);
    }
}

