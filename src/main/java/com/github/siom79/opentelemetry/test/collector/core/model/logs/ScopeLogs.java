package com.github.siom79.opentelemetry.test.collector.core.model.logs;

import java.util.List;

import com.github.siom79.opentelemetry.test.collector.core.model.common.InstrumentationScope;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScopeLogs {
    private String schemaUrl;
    private InstrumentationScope scope;
    private List<LogRecord> logRecords;
}
