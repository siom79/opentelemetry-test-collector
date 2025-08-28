package com.github.siom79.opentelemetry.test.collector.core.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Span {

    public enum SpanKind {
        SPAN_KIND_UNSPECIFIED,
        SPAN_KIND_INTERNAL,
        SPAN_KIND_SERVER,
        SPAN_KIND_CLIENT,
        SPAN_KIND_PRODUCER,
        SPAN_KIND_CONSUMER,
        SPAN_KIND_UNRECOGNIZED
    }

    private String traceId;
    private String spanId;
    private String traceState;
    private String parentSpanId;
    private int flags;
    private String name;
    private SpanKind spanKind;
    private long startTimeUnixMano;
    private long endTimeUnixNano;
    private List<KeyValue> attributes;
    private long droppedAttributesCount;
    private List<Event> events;
    private long droppedEventsCount;
    private Status status;
}
