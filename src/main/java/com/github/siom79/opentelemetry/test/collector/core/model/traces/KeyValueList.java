package com.github.siom79.opentelemetry.test.collector.core.model.traces;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeyValueList {
    @Builder.Default
    private List<KeyValue> keyValueList = new ArrayList<>();
}
