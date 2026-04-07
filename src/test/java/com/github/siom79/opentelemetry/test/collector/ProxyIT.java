package com.github.siom79.opentelemetry.test.collector;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.siom79.opentelemetry.test.collector.core.services.MetricsService;
import com.github.siom79.opentelemetry.test.collector.core.services.TracesService;

import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

/**
 * Verifies the proxy feature: requests sent to the frontend collector (the app
 * under test) are stored in-memory there AND forwarded to a second, upstream
 * collector (a Testcontainer). Both instances must contain the telemetry data.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProxyIT {

    @Container
    static GenericContainer<?> backendCollector = new GenericContainer<>(
            "ghcr.io/siom79/opentelemetry-test-collector:main")
            .withExposedPorts(4317, 4318)
            .waitingFor(Wait.forHttp("/actuator/health")
                    .forPort(4318)
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(3)));

    @DynamicPropertySource
    static void configureProxy(DynamicPropertyRegistry registry) {
        registry.add(
                "com.github.siom79.opentelemetry-test-collector.proxy.endpoint",
                () -> "http://localhost:" + backendCollector.getMappedPort(4318));
    }

    @LocalServerPort
    private int frontendPort;

    @Autowired
    private TracesService tracesService;

    @Autowired
    private MetricsService metricsService;

    private RestClient backendRestClient;

    @BeforeEach
    void setUp() {
        tracesService.clear();
        metricsService.clear();
        backendRestClient = RestClient.create("http://localhost:" + backendCollector.getMappedPort(4318));
        backendRestClient.post().uri("/api/traces/clear").retrieve().toBodilessEntity();
        backendRestClient.post().uri("/api/metrics/clear").retrieve().toBodilessEntity();
    }

    @Test
    void proxiedTrace_isReceivedByBothCollectors() {
        String frontendUrl = "http://localhost:" + frontendPort;
        OtlpHttpSpanExporter exporter = OtlpHttpSpanExporter.builder()
                .setEndpoint(frontendUrl + "/v1/traces")
                .build();
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.builder(exporter).build())
                .build();
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();

        Tracer tracer = openTelemetry.getTracer("proxy-test");
        Span span = tracer.spanBuilder("proxy-test-span").startSpan();
        try {
            span.addEvent("work in proxy test");
        } finally {
            span.end();
            openTelemetry.shutdown();
        }

        // frontend collector must have the span in-memory
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .until(() -> !tracesService.getResourceSpans().isEmpty());
        assertThat(tracesService.getResourceSpans())
                .isNotEmpty();
        assertThat(tracesService.getResourceSpans()
                .getFirst().getScopeSpans().getFirst().getSpans().getFirst().getName())
                .isEqualTo("proxy-test-span");

        // backend (upstream) collector must also have received the span
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            String body = backendRestClient.get()
                    .uri("/api/traces/list")
                    .retrieve()
                    .body(String.class);
            assertThat(body).contains("proxy-test-span");
            assertThat(body).contains("proxy-test");
        });
    }

    @Test
    void proxiedMetric_isReceivedByBothCollectors() {
        String frontendUrl = "http://localhost:" + frontendPort;
        OtlpHttpMetricExporter exporter = OtlpHttpMetricExporter.builder()
                .setEndpoint(frontendUrl + "/v1/metrics")
                .build();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.create(exporter))
                .build();
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .build();

        Meter meter = openTelemetry.getMeter("proxy-test");
        DoubleGauge gauge = meter.gaugeBuilder("proxy.test.gauge").build();
        gauge.set(7.0);
        openTelemetry.shutdown();

        // frontend collector must have the metric in-memory
        Awaitility.await().atMost(15, TimeUnit.SECONDS)
                .until(() -> !metricsService.getMetrics().isEmpty());
        assertThat(metricsService.getMetrics()).isNotEmpty();

        // backend (upstream) collector must also have received the metric
        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String body = backendRestClient.get()
                    .uri("/api/metrics/list")
                    .retrieve()
                    .body(String.class);
            assertThat(body).contains("proxy.test.gauge");
            assertThat(body).contains("7.0");
        });
    }
}
