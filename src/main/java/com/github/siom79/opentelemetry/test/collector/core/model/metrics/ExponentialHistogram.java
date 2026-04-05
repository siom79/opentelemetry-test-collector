package com.github.siom79.opentelemetry.test.collector.core.model.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class ExponentialHistogram extends Data {
    private List<ExponentialHistogramDataPoint> dataPoints;
    private AggregationTemporality aggregationTemporality;
}
