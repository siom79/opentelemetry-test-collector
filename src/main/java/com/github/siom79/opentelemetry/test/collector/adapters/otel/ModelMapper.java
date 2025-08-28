package com.github.siom79.opentelemetry.test.collector.adapters.otel;

import com.github.siom79.opentelemetry.test.collector.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.github.siom79.opentelemetry.test.collector.core.util.HexUtils.bytesToHex;

@Slf4j
@Service
public class ModelMapper {

    public ResourceSpans mapResourceSpans(io.opentelemetry.proto.trace.v1.ResourceSpans input) {
        return ResourceSpans.builder()
                .schemaUrl(input.getSchemaUrl())
                .resource(mapResource(input.getResource()))
                .scopeSpans(mapScopeSpans(input.getScopeSpansList()))
                .build();
    }

    private List<ScopeSpans> mapScopeSpans(List<io.opentelemetry.proto.trace.v1.ScopeSpans> scopeSpansList) {
        return scopeSpansList.stream().map(this::mapScopeSpans).toList();
    }

    private ScopeSpans mapScopeSpans(io.opentelemetry.proto.trace.v1.ScopeSpans s) {
        return ScopeSpans.builder()
                .schemaUrl(s.getSchemaUrl())
                .scope(InstrumentationScope.builder()
                                        .name(s.getScope().getName())
                                        .version(s.getScope().getVersion())
                                        .attributes(mapKeyValueList(s.getScope().getAttributesList()))
                                        .droppedAttributesCount(s.getScope().getDroppedAttributesCount())
                                        .build())
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
                        .attributes(mapKeyValueList(s.getAttributesList()))
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
                        .attributes(mapKeyValueList(e.getAttributesList()))
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

    private Resource mapResource(io.opentelemetry.proto.resource.v1.Resource resource) {
        return Resource.builder()
                .droppedAttributesCount(resource.getDroppedAttributesCount())
                .attributes(mapKeyValueList(resource.getAttributesList()))
                .build();
    }

    private List<KeyValue> mapKeyValueList(List<io.opentelemetry.proto.common.v1.KeyValue> attributesList) {
        return attributesList.stream()
                .map(a -> KeyValue.builder()
                        .key(a.getKey())
                        .value(mapAnyValue(a.getValue()))
                        .build())
                .toList();
    }

    private AnyValue mapAnyValue(io.opentelemetry.proto.common.v1.AnyValue anyValue) {
        if (anyValue.hasBoolValue()) {
            return AnyValue.builder()
                    .type(AnyValue.Type.BOOLEAN)
                    .booleanValue(anyValue.getBoolValue())
                    .build();
        } else if (anyValue.hasBytesValue()) {
            return AnyValue.builder()
                    .type(AnyValue.Type.BYTE)
                    .byteValue(anyValue.getBytesValue().asReadOnlyByteBuffer().array())
                    .build();
        } else if (anyValue.hasDoubleValue()) {
            return AnyValue.builder()
                    .type(AnyValue.Type.DOUBLE)
                    .doubleValue(anyValue.getDoubleValue())
                    .build();
        } else if (anyValue.hasIntValue()) {
            return AnyValue.builder()
                    .type(AnyValue.Type.INT)
                    .integerValue(anyValue.getIntValue())
                    .build();
        } else if (anyValue.hasStringValue()) {
            return AnyValue.builder()
                    .type(AnyValue.Type.STRING)
                    .stringValue(anyValue.getStringValue())
                    .build();
        } else if (anyValue.hasKvlistValue()) {
            return AnyValue.builder()
                    .type(AnyValue.Type.KVLIST)
                    .kvList(mapKeyValueList(anyValue.getKvlistValue().getValuesList()))
                    .build();
        } else {
            log.warn("Unsupported AnyValue type: {}", anyValue);
            return null;
        }
    }
}
