package com.example.orderservice.grpc.security;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GrpcClientSecurityInterceptor implements ClientInterceptor {

    @Value("${app.grpc.auth-token:demo-token}")
    private String authToken;

    @Override
    public <T, R> ClientCall<T, R> interceptCall(
            MethodDescriptor<T, R> method,
            CallOptions callOptions,
            Channel next) {


        return new ForwardingClientCall.SimpleForwardingClientCall<T, R>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<R> responseListener, Metadata headers) {
                Metadata.Key<String> authKey = Metadata.Key.of(
                        "authorization", Metadata.ASCII_STRING_MARSHALLER);
                headers.put(authKey, "Bearer " + authToken);
                super.start(responseListener, headers);
            }
        };
    }
}

