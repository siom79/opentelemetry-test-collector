package com.github.siom79.opentelemetry.test.collector.core.services;

import com.github.siom79.opentelemetry.test.collector.core.model.metrics.ResourceMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MetricsService {

    private final List<ResourceMetrics> metrics = new ArrayList<>();

    public void addMetrics(List<ResourceMetrics> resourceSpans) {
        metrics.addAll(resourceSpans);
    }

    public synchronized List<ResourceMetrics> getMetrics() {
        return metrics;
    }

    public synchronized void clear() {
        metrics.clear();
    }
}
