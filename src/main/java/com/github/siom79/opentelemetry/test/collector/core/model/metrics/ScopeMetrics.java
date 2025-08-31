package com.github.siom79.opentelemetry.test.collector.core.model.metrics;

import com.github.siom79.opentelemetry.test.collector.core.model.common.InstrumentationScope;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScopeMetrics {
    private InstrumentationScope instrumentationScope;
    private List<Metric> metrics;
    private String schemaUrl;
}
