package com.github.siom79.opentelemetry.test.collector.adapters.otel;

import com.github.siom79.opentelemetry.test.collector.core.model.common.AnyValue;
import com.github.siom79.opentelemetry.test.collector.core.model.common.InstrumentationScope;
import com.github.siom79.opentelemetry.test.collector.core.model.common.KeyValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CommonModelMapper {

    public List<KeyValue> mapKeyValueList(List<io.opentelemetry.proto.common.v1.KeyValue> attributesList) {
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

    public InstrumentationScope mapInstrumentationScope(io.opentelemetry.proto.common.v1.InstrumentationScope s) {
        return InstrumentationScope.builder()
                .name(s.getName())
                .version(s.getVersion())
                .attributes(this.mapKeyValueList(s.getAttributesList()))
                .droppedAttributesCount(s.getDroppedAttributesCount())
                .build();
    }
}
