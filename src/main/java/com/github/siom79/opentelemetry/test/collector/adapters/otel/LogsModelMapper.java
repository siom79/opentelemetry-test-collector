package com.github.siom79.opentelemetry.test.collector.adapters.otel;

import static com.github.siom79.opentelemetry.test.collector.core.util.HexUtils.bytesToHex;

import java.util.List;

import org.springframework.stereotype.Service;

import com.github.siom79.opentelemetry.test.collector.core.model.logs.LogRecord;
import com.github.siom79.opentelemetry.test.collector.core.model.logs.ResourceLogs;
import com.github.siom79.opentelemetry.test.collector.core.model.logs.ScopeLogs;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LogsModelMapper {

    private final ResourceModelMapper resourceModelMapper;
    private final CommonModelMapper commonModelMapper;

    public LogsModelMapper(ResourceModelMapper resourceModelMapper,
                           CommonModelMapper commonModelMapper) {
        this.resourceModelMapper = resourceModelMapper;
        this.commonModelMapper = commonModelMapper;
    }

    public ResourceLogs mapResourceLogs(io.opentelemetry.proto.logs.v1.ResourceLogs input) {
        return ResourceLogs.builder()
                .schemaUrl(input.getSchemaUrl())
                .resource(this.resourceModelMapper.mapResource(input.getResource()))
                .scopeLogs(mapScopeLogsList(input.getScopeLogsList()))
                .build();
    }

    private List<ScopeLogs> mapScopeLogsList(List<io.opentelemetry.proto.logs.v1.ScopeLogs> scopeLogsList) {
        return scopeLogsList.stream().map(this::mapScopeLogs).toList();
    }

    private ScopeLogs mapScopeLogs(io.opentelemetry.proto.logs.v1.ScopeLogs s) {
        return ScopeLogs.builder()
                .schemaUrl(s.getSchemaUrl())
                .scope(this.commonModelMapper.mapInstrumentationScope(s.getScope()))
                .logRecords(mapLogRecords(s.getLogRecordsList()))
                .build();
    }

    private List<LogRecord> mapLogRecords(List<io.opentelemetry.proto.logs.v1.LogRecord> logRecordsList) {
        return logRecordsList.stream()
                .map(r -> LogRecord.builder()
                        .timeUnixNano(r.getTimeUnixNano())
                        .observedTimeUnixNano(r.getObservedTimeUnixNano())
                        .severityNumber(mapSeverityNumber(r.getSeverityNumber()))
                        .severityText(r.getSeverityText())
                        .body(this.commonModelMapper.mapAnyValue(r.getBody()))
                        .attributes(this.commonModelMapper.mapKeyValueList(r.getAttributesList()))
                        .droppedAttributesCount(r.getDroppedAttributesCount())
                        .flags(r.getFlags())
                        .traceId(bytesToHex(r.getTraceId().toByteArray()))
                        .spanId(bytesToHex(r.getSpanId().toByteArray()))
                        .eventName(r.getEventName())
                        .build())
                .toList();
    }

    private LogRecord.SeverityNumber mapSeverityNumber(io.opentelemetry.proto.logs.v1.SeverityNumber severityNumber) {
        return switch (severityNumber) {
            case SEVERITY_NUMBER_TRACE -> LogRecord.SeverityNumber.SEVERITY_NUMBER_TRACE;
            case SEVERITY_NUMBER_TRACE2 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_TRACE2;
            case SEVERITY_NUMBER_TRACE3 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_TRACE3;
            case SEVERITY_NUMBER_TRACE4 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_TRACE4;
            case SEVERITY_NUMBER_DEBUG -> LogRecord.SeverityNumber.SEVERITY_NUMBER_DEBUG;
            case SEVERITY_NUMBER_DEBUG2 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_DEBUG2;
            case SEVERITY_NUMBER_DEBUG3 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_DEBUG3;
            case SEVERITY_NUMBER_DEBUG4 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_DEBUG4;
            case SEVERITY_NUMBER_INFO -> LogRecord.SeverityNumber.SEVERITY_NUMBER_INFO;
            case SEVERITY_NUMBER_INFO2 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_INFO2;
            case SEVERITY_NUMBER_INFO3 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_INFO3;
            case SEVERITY_NUMBER_INFO4 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_INFO4;
            case SEVERITY_NUMBER_WARN -> LogRecord.SeverityNumber.SEVERITY_NUMBER_WARN;
            case SEVERITY_NUMBER_WARN2 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_WARN2;
            case SEVERITY_NUMBER_WARN3 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_WARN3;
            case SEVERITY_NUMBER_WARN4 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_WARN4;
            case SEVERITY_NUMBER_ERROR -> LogRecord.SeverityNumber.SEVERITY_NUMBER_ERROR;
            case SEVERITY_NUMBER_ERROR2 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_ERROR2;
            case SEVERITY_NUMBER_ERROR3 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_ERROR3;
            case SEVERITY_NUMBER_ERROR4 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_ERROR4;
            case SEVERITY_NUMBER_FATAL -> LogRecord.SeverityNumber.SEVERITY_NUMBER_FATAL;
            case SEVERITY_NUMBER_FATAL2 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_FATAL2;
            case SEVERITY_NUMBER_FATAL3 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_FATAL3;
            case SEVERITY_NUMBER_FATAL4 -> LogRecord.SeverityNumber.SEVERITY_NUMBER_FATAL4;
            default -> {
                log.warn("Unsupported SeverityNumber: {}", severityNumber);
                yield LogRecord.SeverityNumber.SEVERITY_NUMBER_UNSPECIFIED;
            }
        };
    }
}
