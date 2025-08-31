package com.github.siom79.opentelemetry.test.collector.core.services;

import com.github.siom79.opentelemetry.test.collector.core.model.traces.ResourceSpans;
import com.google.common.collect.EvictingQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TracesService {

    @Value("${com.github.siom79.opentelemetry-test-collector.traces.cache.size}")
    private int cacheSize;
    private final EvictingQueue<ResourceSpans> traces = EvictingQueue.create(cacheSize);

    public synchronized void addResourceSpans(List<ResourceSpans> resourceSpans) {
        traces.addAll(resourceSpans);
    }

    public synchronized List<ResourceSpans> getResourceSpans() {
        return traces.stream().toList();
    }

    public synchronized void clear() {
        traces.clear();
    }
}
