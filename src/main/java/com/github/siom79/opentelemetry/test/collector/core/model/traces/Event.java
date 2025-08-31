package com.github.siom79.opentelemetry.test.collector.core.model.traces;

import com.github.siom79.opentelemetry.test.collector.core.model.common.KeyValue;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private long timeUnixNano;
    private String name;
    private List<KeyValue> attributes;
    private int droppedAttributesCount;
}
