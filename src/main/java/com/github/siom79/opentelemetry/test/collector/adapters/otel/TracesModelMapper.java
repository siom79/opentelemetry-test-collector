package com.github.siom79.opentelemetry.test.collector.adapters.otel;

import com.github.siom79.opentelemetry.test.collector.core.model.common.InstrumentationScope;
import com.github.siom79.opentelemetry.test.collector.core.model.traces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.github.siom79.opentelemetry.test.collector.core.util.HexUtils.bytesToHex;

@Slf4j
@Service
public class TracesModelMapper {

    private final ResourceModelMapper resourceModelMapper;
    private final CommonModelMapper commonModelMapper;

    public TracesModelMapper(ResourceModelMapper resourceModelMapper,
                             CommonModelMapper commonModelMapper) {
        this.resourceModelMapper = resourceModelMapper;
        this.commonModelMapper = commonModelMapper;
    }

    public ResourceSpans mapResourceSpans(io.opentelemetry.proto.trace.v1.ResourceSpans input) {
        return ResourceSpans.builder()
                .schemaUrl(input.getSchemaUrl())
                .resource(this.resourceModelMapper.mapResource(input.getResource()))
                .scopeSpans(mapScopeSpans(input.getScopeSpansList()))
                .build();
    }

    private List<ScopeSpans> mapScopeSpans(List<io.opentelemetry.proto.trace.v1.ScopeSpans> scopeSpansList) {
        return scopeSpansList.stream().map(this::mapScopeSpans).toList();
    }

    private ScopeSpans mapScopeSpans(io.opentelemetry.proto.trace.v1.ScopeSpans s) {
        return ScopeSpans.builder()
                .schemaUrl(s.getSchemaUrl())
                .scope(this.commonModelMapper.mapInstrumentationScope(s.getScope()))
                .spans(mapSpans(s.getSpansList()))
                .build();
    }

    private List<Span> mapSpans(List<io.opentelemetry.proto.trace.v1.Span> spansList) {
        return spansList.stream()
                .map(s -> Span.builder()
                        .traceId(bytesToHex(s.getTraceId().toByteArray()))
                        .spanId(bytesToHex(s.getSpanId().toByteArray()))
                        .traceState(s.getTraceState())
                        .parentSpanId(bytesToHex(s.getParentSpanId().toByteArray()))
                        .flags(s.getFlags())
                        .name(s.getName())
                        .spanKind(mapSpanKind(s.getKind()))
                        .startTimeUnixMano(s.getStartTimeUnixNano())
                        .endTimeUnixNano(s.getEndTimeUnixNano())
                        .attributes(this.commonModelMapper.mapKeyValueList(s.getAttributesList()))
                        .droppedAttributesCount(s.getDroppedAttributesCount())
                        .events(mapEvents(s.getEventsList()))
                        .droppedEventsCount(s.getDroppedEventsCount())
                        .status(mapStatus(s.getStatus()))
                        .build())
                .toList();
    }

    private Status mapStatus(io.opentelemetry.proto.trace.v1.Status status) {
        return Status.builder()
                .message(status.getMessage())
                .code(mapStatusCode(status.getCode()))
                .build();
    }

    private Status.StatusCode mapStatusCode(io.opentelemetry.proto.trace.v1.Status.StatusCode code) {
        switch(code) {
            case STATUS_CODE_OK -> {
                return Status.StatusCode.STATUS_CODE_OK;
            }
            case STATUS_CODE_ERROR -> {
                return Status.StatusCode.STATUS_CODE_ERROR;
            }
            case STATUS_CODE_UNSET -> {
                return Status.StatusCode.STATUS_CODE_UNSET;
            }
            default -> {
                log.warn("Unsupported StatusCode: {}", code);
                return null;
            }
        }
    }

    private List<Event> mapEvents(List<io.opentelemetry.proto.trace.v1.Span.Event> eventsList) {
        return eventsList.stream()
                .map(e -> Event.builder()
                        .timeUnixNano(e.getTimeUnixNano())
                        .name(e.getName())
                        .attributes(this.commonModelMapper.mapKeyValueList(e.getAttributesList()))
                        .droppedAttributesCount(e.getDroppedAttributesCount())
                        .build())
                .toList();
    }

    private Span.SpanKind mapSpanKind(io.opentelemetry.proto.trace.v1.Span.SpanKind kind) {
        switch (kind) {
            case SPAN_KIND_CLIENT -> {
                return Span.SpanKind.SPAN_KIND_CLIENT;
            }
            case SPAN_KIND_INTERNAL -> {
                return Span.SpanKind.SPAN_KIND_INTERNAL;
            }
            case SPAN_KIND_SERVER -> {
                return Span.SpanKind.SPAN_KIND_SERVER;
            }
            case SPAN_KIND_PRODUCER -> {
                return Span.SpanKind.SPAN_KIND_PRODUCER;
            }
            case SPAN_KIND_CONSUMER -> {
                return Span.SpanKind.SPAN_KIND_CONSUMER;
            }
            default -> {
                log.warn("Unsupported span kind: {}", kind);
                return null;
            }
        }
    }
}
