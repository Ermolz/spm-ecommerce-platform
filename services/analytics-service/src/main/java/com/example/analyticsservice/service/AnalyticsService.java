package com.example.analyticsservice.service;

import com.example.analyticsservice.domain.AnalyticsEvent;
import com.example.analyticsservice.repo.AnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsEventRepository repo;

    @Transactional
    public AnalyticsEvent save(Long orderId, String type, String priority) {
        AnalyticsEvent e = AnalyticsEvent.builder()
                .orderId(orderId)
                .type(type)
                .priority(priority)
                .receivedAt(OffsetDateTime.now())
                .build();
        return repo.save(e);
    }
}
