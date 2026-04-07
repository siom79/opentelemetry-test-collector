package com.github.siom79.opentelemetry.test.collector.adapters.otel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.siom79.opentelemetry.test.collector.core.model.common.AnyValue;
import com.github.siom79.opentelemetry.test.collector.core.model.logs.LogRecord;
import com.github.siom79.opentelemetry.test.collector.core.model.logs.ResourceLogs;
import com.google.protobuf.ByteString;

class LogsModelMapperTest {

    private LogsModelMapper mapper;

    @BeforeEach
    void setUp() {
        CommonModelMapper commonModelMapper = new CommonModelMapper();
        ResourceModelMapper resourceModelMapper = new ResourceModelMapper(commonModelMapper);
        mapper = new LogsModelMapper(resourceModelMapper, commonModelMapper);
    }

    @Test
    void mapResourceLogs_mapsSchemaUrlAndScopeLogs() {
        io.opentelemetry.proto.logs.v1.ResourceLogs input =
                io.opentelemetry.proto.logs.v1.ResourceLogs.newBuilder()
                        .setSchemaUrl("https://example.com/schema")
                        .addScopeLogs(io.opentelemetry.proto.logs.v1.ScopeLogs.newBuilder()
                                .setScope(io.opentelemetry.proto.common.v1.InstrumentationScope.newBuilder()
                                        .setName("my-logger")
                                        .setVersion("2.0.0")
                                        .build())
                                .setSchemaUrl("https://scope.schema")
                                .build())
                        .build();

        ResourceLogs result = mapper.mapResourceLogs(input);

        assertThat(result.getSchemaUrl()).isEqualTo("https://example.com/schema");
        assertThat(result.getScopeLogs()).hasSize(1);
        assertThat(result.getScopeLogs().getFirst().getSchemaUrl()).isEqualTo("https://scope.schema");
        assertThat(result.getScopeLogs().getFirst().getScope().getName()).isEqualTo("my-logger");
        assertThat(result.getScopeLogs().getFirst().getScope().getVersion()).isEqualTo("2.0.0");
    }

    @Test
    void mapResourceLogs_mapsLogRecordTimestamps() {
        io.opentelemetry.proto.logs.v1.ResourceLogs input = resourceLogsWithRecord(
                io.opentelemetry.proto.logs.v1.LogRecord.newBuilder()
                        .setTimeUnixNano(1_000_000L)
                        .setObservedTimeUnixNano(2_000_000L)
                        .build());

        ResourceLogs result = mapper.mapResourceLogs(input);

        LogRecord record = firstRecord(result);
        assertThat(record.getTimeUnixNano()).isEqualTo(1_000_000L);
        assertThat(record.getObservedTimeUnixNano()).isEqualTo(2_000_000L);
    }

    @Test
    void mapResourceLogs_mapsSeverityNumberAndText() {
        io.opentelemetry.proto.logs.v1.ResourceLogs input = resourceLogsWithRecord(
                io.opentelemetry.proto.logs.v1.LogRecord.newBuilder()
                        .setSeverityNumber(io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_WARN)
                        .setSeverityText("WARN")
                        .build());

        ResourceLogs result = mapper.mapResourceLogs(input);

        LogRecord record = firstRecord(result);
        assertThat(record.getSeverityNumber()).isEqualTo(LogRecord.SeverityNumber.SEVERITY_NUMBER_WARN);
        assertThat(record.getSeverityText()).isEqualTo("WARN");
    }

    @Test
    void mapResourceLogs_mapsAllSeverityNumbers() {
        for (io.opentelemetry.proto.logs.v1.SeverityNumber protoSeverity :
                io.opentelemetry.proto.logs.v1.SeverityNumber.values()) {
            if (protoSeverity == io.opentelemetry.proto.logs.v1.SeverityNumber.UNRECOGNIZED) continue;

            io.opentelemetry.proto.logs.v1.ResourceLogs input = resourceLogsWithRecord(
                    io.opentelemetry.proto.logs.v1.LogRecord.newBuilder()
                            .setSeverityNumber(protoSeverity)
                            .build());

            ResourceLogs result = mapper.mapResourceLogs(input);
            assertThat(firstRecord(result).getSeverityNumber()).isNotNull();
        }
    }

    @Test
    void mapResourceLogs_mapsStringBody() {
        io.opentelemetry.proto.logs.v1.ResourceLogs input = resourceLogsWithRecord(
                io.opentelemetry.proto.logs.v1.LogRecord.newBuilder()
                        .setBody(io.opentelemetry.proto.common.v1.AnyValue.newBuilder()
                                .setStringValue("Hello, World!")
                                .build())
                        .build());

        ResourceLogs result = mapper.mapResourceLogs(input);

        LogRecord record = firstRecord(result);
        assertThat(record.getBody()).isNotNull();
        assertThat(record.getBody().getType()).isEqualTo(AnyValue.Type.STRING);
        assertThat(record.getBody().getStringValue()).isEqualTo("Hello, World!");
    }

    @Test
    void mapResourceLogs_mapsAttributes() {
        io.opentelemetry.proto.logs.v1.ResourceLogs input = resourceLogsWithRecord(
                io.opentelemetry.proto.logs.v1.LogRecord.newBuilder()
                        .addAttributes(io.opentelemetry.proto.common.v1.KeyValue.newBuilder()
                                .setKey("service.name")
                                .setValue(io.opentelemetry.proto.common.v1.AnyValue.newBuilder()
                                        .setStringValue("my-service")
                                        .build())
                                .build())
                        .setDroppedAttributesCount(2)
                        .build());

        ResourceLogs result = mapper.mapResourceLogs(input);

        LogRecord record = firstRecord(result);
        assertThat(record.getAttributes()).hasSize(1);
        assertThat(record.getAttributes().getFirst().getKey()).isEqualTo("service.name");
        assertThat(record.getAttributes().getFirst().getValue().getStringValue()).isEqualTo("my-service");
        assertThat(record.getDroppedAttributesCount()).isEqualTo(2);
    }

    @Test
    void mapResourceLogs_mapsTraceIdAndSpanIdAsHex() {
        byte[] traceIdBytes = new byte[]{0x40, 0x5c, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05,
                0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d};
        byte[] spanIdBytes = new byte[]{0x60, 0x34, 0x0b, 0x01, 0x02, 0x03, 0x04, 0x05};

        io.opentelemetry.proto.logs.v1.ResourceLogs input = resourceLogsWithRecord(
                io.opentelemetry.proto.logs.v1.LogRecord.newBuilder()
                        .setTraceId(ByteString.copyFrom(traceIdBytes))
                        .setSpanId(ByteString.copyFrom(spanIdBytes))
                        .build());

        ResourceLogs result = mapper.mapResourceLogs(input);

        LogRecord record = firstRecord(result);
        assertThat(record.getTraceId()).isEqualTo("405c000102030405060708090a0b0c0d");
        assertThat(record.getSpanId()).isEqualTo("60340b0102030405");
    }

    @Test
    void mapResourceLogs_mapsFlags() {
        io.opentelemetry.proto.logs.v1.ResourceLogs input = resourceLogsWithRecord(
                io.opentelemetry.proto.logs.v1.LogRecord.newBuilder()
                        .setFlags(257)
                        .build());

        ResourceLogs result = mapper.mapResourceLogs(input);

        assertThat(firstRecord(result).getFlags()).isEqualTo(257);
    }

    @Test
    void mapResourceLogs_mapsEventName() {
        io.opentelemetry.proto.logs.v1.ResourceLogs input = resourceLogsWithRecord(
                io.opentelemetry.proto.logs.v1.LogRecord.newBuilder()
                        .setEventName("my.event")
                        .build());

        ResourceLogs result = mapper.mapResourceLogs(input);

        assertThat(firstRecord(result).getEventName()).isEqualTo("my.event");
    }

    @Test
    void mapResourceLogs_emptyResourceLogs_returnEmptyScopeLogs() {
        io.opentelemetry.proto.logs.v1.ResourceLogs input =
                io.opentelemetry.proto.logs.v1.ResourceLogs.newBuilder().build();

        ResourceLogs result = mapper.mapResourceLogs(input);

        assertThat(result.getScopeLogs()).isEmpty();
    }

    // --- helpers ---

    private io.opentelemetry.proto.logs.v1.ResourceLogs resourceLogsWithRecord(
            io.opentelemetry.proto.logs.v1.LogRecord logRecord) {
        return io.opentelemetry.proto.logs.v1.ResourceLogs.newBuilder()
                .addScopeLogs(io.opentelemetry.proto.logs.v1.ScopeLogs.newBuilder()
                        .setScope(io.opentelemetry.proto.common.v1.InstrumentationScope.newBuilder()
                                .setName("test-scope")
                                .build())
                        .addLogRecords(logRecord)
                        .build())
                .build();
    }

    private LogRecord firstRecord(ResourceLogs resourceLogs) {
        return resourceLogs.getScopeLogs().getFirst().getLogRecords().getFirst();
    }
}
