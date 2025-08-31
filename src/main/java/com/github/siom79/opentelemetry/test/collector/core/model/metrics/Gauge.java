package com.github.siom79.opentelemetry.test.collector.core.model.metrics;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class Gauge extends Data {
    private List<NumberDataPoint> dataPoints;
}
