package com.github.siom79.opentelemetry.test.collector.core.model.traces;

import com.github.siom79.opentelemetry.test.collector.core.model.common.KeyValue;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Link {
    private String traceId;
    private String spanId;
    private String traceState;
    private List<KeyValue> attributes;
    private int droppedAttributesCount;
    private int flags;
}
