package com.github.siom79.opentelemetry.test.collector.core.model.metrics;

import com.github.siom79.opentelemetry.test.collector.core.model.common.KeyValue;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NumberDataPoint {
    private List<KeyValue> attributes;
    private long startTimeUnixNano;
    private long timeUnixNano;
    private Number value;
    private int flags;
}
