package com.github.siom79.opentelemetry.test.collector.core.model.metrics;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Metric {
    private String name;
    private String description;
    private String unit;
}
