package com.github.siom79.opentelemetry.test.collector;

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.github.siom79.opentelemetry.test.collector.core.model.logs.LogRecord;
import com.github.siom79.opentelemetry.test.collector.core.services.LogsService;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class HttpLogsIT {

    private OpenTelemetrySdk openTelemetry;

    @Autowired
    private LogsService logsService;

    @BeforeEach
    void beforeEach() {
        logsService.clear();
        OtlpHttpLogRecordExporter exporter = OtlpHttpLogRecordExporter.builder()
                .setEndpoint("http://localhost:4318/v1/logs")
                .build();
        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
                .addLogRecordProcessor(SimpleLogRecordProcessor.create(exporter))
                .build();
        openTelemetry = OpenTelemetrySdk.builder()
                .setLoggerProvider(loggerProvider)
                .build();
    }

    @Test
    void exportedLogRecord_isReceivedByCollector() {
        Logger logger = openTelemetry.getLogsBridge().get("example-app");
        logger.logRecordBuilder()
                .setSeverity(Severity.INFO)
                .setSeverityText("INFO")
                .setBody("test log message via HTTP")
                .emit();
        openTelemetry.shutdown();

        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .until(() -> !logsService.getResourceLogs().isEmpty());

        Assertions.assertThat(logsService.getResourceLogs()).isNotEmpty();
        LogRecord record = logsService.getResourceLogs().getFirst()
                .getScopeLogs().getFirst()
                .getLogRecords().getFirst();
        Assertions.assertThat(record.getSeverityText()).isEqualTo("INFO");
        Assertions.assertThat(record.getBody().getStringValue()).isEqualTo("test log message via HTTP");
    }
}
