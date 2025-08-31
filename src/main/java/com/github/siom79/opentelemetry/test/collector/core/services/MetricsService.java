package com.github.siom79.opentelemetry.test.collector.core.services;

import com.github.siom79.opentelemetry.test.collector.core.model.metrics.ResourceMetrics;
import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MetricsService {

    @Value("${com.github.siom79.opentelemetry-test-collector.metrics.cache.size}")
    private int cacheSize;
    private final EvictingQueue<ResourceMetrics> metrics = EvictingQueue.create(cacheSize);

    public synchronized void addMetrics(List<ResourceMetrics> resourceSpans) {
        metrics.addAll(resourceSpans);
    }

    public synchronized List<ResourceMetrics> getMetrics() {
        return metrics.stream().toList();
    }

    public synchronized void clear() {
        metrics.clear();
    }
}
