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
public class Sum extends Data {
    private List<NumberDataPoint> dataPoints;
    private AggregationTemporality aggregationTemporality;
}
