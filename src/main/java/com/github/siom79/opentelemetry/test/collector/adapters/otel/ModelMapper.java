package com.github.siom79.opentelemetry.test.collector.adapters.otel;

import com.github.siom79.opentelemetry.test.collector.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
                .map(s -> Span.builder().traceId(s.getTraceId().toStringUtf8())
                        .build())
                .toList();
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
