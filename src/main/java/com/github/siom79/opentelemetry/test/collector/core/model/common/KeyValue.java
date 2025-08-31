package com.github.siom79.opentelemetry.test.collector.core.model.common;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeyValue {
    private String key;
    private AnyValue value;
}
