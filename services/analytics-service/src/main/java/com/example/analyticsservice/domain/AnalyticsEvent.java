package com.example.analyticsservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "analytics_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Order identifier from the event payload
    private Long orderId;

    // Event type: ORDER_CREATED / ORDER_RESERVED / ORDER_REJECTED
    private String type;

    // Event priority: LOW / MEDIUM / HIGH
    private String priority;

    // When this service received the event
    @Builder.Default
    private OffsetDateTime receivedAt = OffsetDateTime.now();
}
