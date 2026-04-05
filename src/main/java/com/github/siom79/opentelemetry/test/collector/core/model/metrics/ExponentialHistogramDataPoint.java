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
public class ExponentialHistogramDataPoint {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Buckets {
        private int offset;
        private List<Long> bucketCounts;
    }

    private List<KeyValue> attributes;
    private long startTimeUnixNano;
    private long timeUnixNano;
    private long count;
    private Double sum;
    private int scale;
    private long zeroCount;
    private Buckets positive;
    private Buckets negative;
    private int flags;
    private List<Exemplar> exemplars;
    private Double min;
    private Double max;
    private double zeroThreshold;
}
