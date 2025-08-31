package com.github.siom79.opentelemetry.test.collector.core.model.metrics;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.siom79.opentelemetry.test.collector.core.model.common.KeyValue;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistogramDataPoint {
    private List<KeyValue> attributes;
    private long startTimeUnixNano;
    private long timeUnixNano;
    private long count;
    private Double sum;
    private List<Long> bucketCounts;
    private List<Double> explicitBounds;
    private int flags;
    private Double min;
    private Double max;
}
