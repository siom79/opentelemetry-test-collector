package com.github.siom79.opentelemetry.test.collector.core.services;

import com.github.siom79.opentelemetry.test.collector.core.model.traces.ResourceSpans;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TracesService {

    private final List<ResourceSpans> traces = new ArrayList<>();

    public void addResourceSpans(List<ResourceSpans> resourceSpans) {
        traces.addAll(resourceSpans);
    }

    public synchronized List<ResourceSpans> getResourceSpans() {
        return traces;
    }

    public synchronized void clear() {
        traces.clear();
    }
}
