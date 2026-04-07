package com.github.siom79.opentelemetry.test.collector.core.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.siom79.opentelemetry.test.collector.core.model.logs.ResourceLogs;
import com.google.common.collect.EvictingQueue;

import jakarta.annotation.PostConstruct;

@Service
public class LogsService {

    @Value("${com.github.siom79.opentelemetry-test-collector.logs.cache.size}")
    private int cacheSize;
    private EvictingQueue<ResourceLogs> logs;

    @PostConstruct
    public void postConstruct() {
        logs = EvictingQueue.create(cacheSize);
    }

    public synchronized void addResourceLogs(List<ResourceLogs> resourceLogs) {
        logs.addAll(resourceLogs);
    }

    public synchronized List<ResourceLogs> getResourceLogs() {
        return logs.stream().toList();
    }

    public synchronized void clear() {
        logs.clear();
    }
}
