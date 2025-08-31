package com.github.siom79.opentelemetry.test.collector.core.model.traces;

import com.github.siom79.opentelemetry.test.collector.core.model.resource.Resource;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceSpans {
    private String schemaUrl;
    private Resource resource;
    private List<ScopeSpans> scopeSpans;
}
