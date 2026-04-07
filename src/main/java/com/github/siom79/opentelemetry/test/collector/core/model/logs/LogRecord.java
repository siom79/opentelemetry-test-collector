package com.github.siom79.opentelemetry.test.collector.core.model.logs;

import java.util.List;

import com.github.siom79.opentelemetry.test.collector.core.model.common.AnyValue;
import com.github.siom79.opentelemetry.test.collector.core.model.common.KeyValue;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogRecord {

    public enum SeverityNumber {
        SEVERITY_NUMBER_UNSPECIFIED,
        SEVERITY_NUMBER_TRACE,
        SEVERITY_NUMBER_TRACE2,
        SEVERITY_NUMBER_TRACE3,
        SEVERITY_NUMBER_TRACE4,
        SEVERITY_NUMBER_DEBUG,
        SEVERITY_NUMBER_DEBUG2,
        SEVERITY_NUMBER_DEBUG3,
        SEVERITY_NUMBER_DEBUG4,
        SEVERITY_NUMBER_INFO,
        SEVERITY_NUMBER_INFO2,
        SEVERITY_NUMBER_INFO3,
        SEVERITY_NUMBER_INFO4,
        SEVERITY_NUMBER_WARN,
        SEVERITY_NUMBER_WARN2,
        SEVERITY_NUMBER_WARN3,
        SEVERITY_NUMBER_WARN4,
        SEVERITY_NUMBER_ERROR,
        SEVERITY_NUMBER_ERROR2,
        SEVERITY_NUMBER_ERROR3,
        SEVERITY_NUMBER_ERROR4,
        SEVERITY_NUMBER_FATAL,
        SEVERITY_NUMBER_FATAL2,
        SEVERITY_NUMBER_FATAL3,
        SEVERITY_NUMBER_FATAL4,
        UNRECOGNIZED
    }

    private long timeUnixNano;
    private long observedTimeUnixNano;
    private SeverityNumber severityNumber;
    private String severityText;
    private AnyValue body;
    private List<KeyValue> attributes;
    private long droppedAttributesCount;
    private int flags;
    private String traceId;
    private String spanId;
    private String eventName;
}
