package com.example.analyticsservice.api;

import com.example.analyticsservice.domain.AnalyticsEvent;
import com.example.analyticsservice.repo.AnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsEventRepository repo;

    @GetMapping("/events")
    public ResponseEntity<List<AnalyticsEvent>> list(@RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(repo.findAll(PageRequest.of(0, Math.max(1, Math.min(size, 100))))
                .getContent());
    }
}