package com.github.siom79.opentelemetry.test.collector.core.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Span {
    private String traceId;
}
