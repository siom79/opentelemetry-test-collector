package com.github.siom79.opentelemetry.test.collector.core.model.metrics;

import com.github.siom79.opentelemetry.test.collector.core.model.common.KeyValue;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SummaryDataPoint {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValueAtQuantile {
        private double quantile;
        private double value;
    }

    private List<KeyValue> attributes;
    private long startTimeUnixNano;
    private long timeUnixNano;
    private long count;
    private double sum;
    private List<ValueAtQuantile> quantileValues;
    private int flags;
}
