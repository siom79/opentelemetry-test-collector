package com.github.siom79.opentelemetry.test.collector.core.model.logs;

import java.util.List;

import com.github.siom79.opentelemetry.test.collector.core.model.resource.Resource;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceLogs {
    private String schemaUrl;
    private Resource resource;
    private List<ScopeLogs> scopeLogs;
}
