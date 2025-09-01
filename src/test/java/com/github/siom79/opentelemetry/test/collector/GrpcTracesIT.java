package com.github.siom79.opentelemetry.test.collector;

import com.github.siom79.opentelemetry.test.collector.core.services.TracesService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class GrpcTracesIT {

    private OpenTelemetrySdk openTelemetry;
    @Autowired
    private TracesService tracesService;

    @BeforeEach
    void beforeEach() {
        OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:4317/v1/traces")
                .build();
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.builder(exporter).build())
                .build();
        openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
    }

    @Test
    void test() {
        Tracer tracer = openTelemetry.getTracer("example-app");
        String spanName = "example-span";
        Span span = tracer.spanBuilder(spanName).startSpan();
        try {
            span.addEvent("doing work...");
        } finally {
            span.end();
            openTelemetry.shutdown();
        }

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> !tracesService.getResourceSpans().isEmpty());
        Assertions.assertThat(tracesService.getResourceSpans()).isNotEmpty();
        Assertions.assertThat(tracesService.getResourceSpans().getFirst().getScopeSpans().getFirst().getSpans().getFirst().getName()).isEqualTo(spanName);
    }
}
