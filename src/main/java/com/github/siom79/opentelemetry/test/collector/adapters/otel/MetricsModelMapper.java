package com.github.siom79.opentelemetry.test.collector.adapters.otel;

import com.github.siom79.opentelemetry.test.collector.core.model.metrics.Metric;
import com.github.siom79.opentelemetry.test.collector.core.model.metrics.ResourceMetrics;
import com.github.siom79.opentelemetry.test.collector.core.model.metrics.ScopeMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MetricsModelMapper {

    private final ResourceModelMapper resourceModelMapper;
    private final CommonModelMapper commonModelMapper;

    public MetricsModelMapper(ResourceModelMapper resourceModelMapper,
                              CommonModelMapper commonModelMapper) {
        this.resourceModelMapper = resourceModelMapper;
        this.commonModelMapper = commonModelMapper;
    }

    public ResourceMetrics map(io.opentelemetry.proto.metrics.v1.ResourceMetrics rm) {
        return ResourceMetrics.builder()
                .resource(resourceModelMapper.mapResource(rm.getResource()))
                .scopeMetrics(mapScopeMetrics(rm.getScopeMetricsList()))
                .schemaUrl(rm.getSchemaUrl())
                .build();
    }

    private List<ScopeMetrics> mapScopeMetrics(List<io.opentelemetry.proto.metrics.v1.ScopeMetrics> scopeMetricsList) {
        return scopeMetricsList.stream().map(this::mapScopeMetric).toList();
    }

    private ScopeMetrics mapScopeMetric(io.opentelemetry.proto.metrics.v1.ScopeMetrics sm) {
        return ScopeMetrics.builder()
                .instrumentationScope(this.commonModelMapper.mapInstrumentationScope(sm.getScope()))
                .metrics(mapMetrics(sm.getMetricsList()))
                .schemaUrl(sm.getSchemaUrl())
                .build();
    }

    private List<Metric> mapMetrics(List<io.opentelemetry.proto.metrics.v1.Metric> metricsList) {
        return metricsList.stream().map(this::mapMetric).toList();
    }

    private Metric mapMetric(io.opentelemetry.proto.metrics.v1.Metric m) {
        return Metric.builder()
                .name(m.getName())
                .description(m.getDescription())
                .unit(m.getUnit())
                .build();
    }
}
