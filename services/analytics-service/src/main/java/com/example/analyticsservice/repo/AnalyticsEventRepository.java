package com.example.analyticsservice.repo;

import com.example.analyticsservice.domain.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> { }