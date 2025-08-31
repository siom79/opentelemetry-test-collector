package com.github.siom79.opentelemetry.test.collector.adapters.otel;

import com.github.siom79.opentelemetry.test.collector.core.model.metrics.ResourceMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MetricsModelMapper {

    public ResourceMetrics map(io.opentelemetry.proto.metrics.v1.ResourceMetrics rm) {
        return ResourceMetrics.builder()
                .schemaUrl(rm.getSchemaUrl())
                .build();
    }
}
