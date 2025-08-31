package com.github.siom79.opentelemetry.test.collector.core.model.traces;

import com.github.siom79.opentelemetry.test.collector.core.model.common.InstrumentationScope;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScopeSpans {
    private InstrumentationScope scope;
    private List<Span> spans;
    private String schemaUrl;
}
