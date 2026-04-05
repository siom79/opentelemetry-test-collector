package com.github.siom79.opentelemetry.test.collector.core.model.metrics;

import com.github.siom79.opentelemetry.test.collector.core.model.common.KeyValue;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Exemplar {
    private List<KeyValue> filteredAttributes;
    private long timeUnixNano;
    private Number value;
    private String spanId;
    private String traceId;
}
