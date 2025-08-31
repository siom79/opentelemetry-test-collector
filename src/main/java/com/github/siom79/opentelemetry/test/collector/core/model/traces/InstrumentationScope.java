package com.github.siom79.opentelemetry.test.collector.core.model.traces;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstrumentationScope {
    private String name;
    private String version;
    private List<KeyValue> attributes;
    private int droppedAttributesCount;
}
