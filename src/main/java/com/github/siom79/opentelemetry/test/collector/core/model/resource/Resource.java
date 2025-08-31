package com.github.siom79.opentelemetry.test.collector.core.model.resource;

import com.github.siom79.opentelemetry.test.collector.core.model.common.KeyValue;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Resource {
    @Builder.Default
    private List<KeyValue> attributes = new ArrayList<>();
    private int droppedAttributesCount;
}
