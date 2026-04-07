package com.github.siom79.opentelemetry.test.collector;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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

@Testcontainers
@Ignore
class TestcontainersIT {

    @Container
    static GenericContainer<?> collector = new GenericContainer<>(
            "ghcr.io/siom79/opentelemetry-test-collector:main")
            .withExposedPorts(4317, 4318)
            .waitingFor(Wait.forHttp("/actuator/health")
                    .forPort(4318)
                    .forStatusCode(200))
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("TC")));

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://" + collector.getHost() + ":" + collector.getMappedPort(4318);
        restClient = RestClient.create(baseUrl);
        restClient.post().uri("/api/traces/clear").retrieve().toBodilessEntity();
        restClient.post().uri("/api/metrics/clear").retrieve().toBodilessEntity();
    }

    @Test
    void exportedTrace_isAccessibleViaRestApi() {
        String collectorBaseUrl = "http://" + collector.getHost() + ":" + collector.getMappedPort(4318);
        OtlpHttpSpanExporter exporter = OtlpHttpSpanExporter.builder()
                .setEndpoint(collectorBaseUrl + "/v1/traces")
                .build();
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.builder(exporter).build())
                .build();
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();

        Tracer tracer = openTelemetry.getTracer("testcontainers-example");
        Span span = tracer.spanBuilder("my-test-span").startSpan();
        try {
            span.addEvent("doing work...");
        } finally {
            span.end();
            openTelemetry.shutdown();
        }

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            String body = restClient.get()
                    .uri("/api/traces/list")
                    .retrieve()
                    .body(String.class);

            assertThat(body).isNotNull();
            assertThat(body).contains("my-test-span");
            assertThat(body).contains("testcontainers-example");
        });
    }

    @Test
    void exportedMetric_isAccessibleViaRestApi() {
        String collectorBaseUrl = "http://" + collector.getHost() + ":" + collector.getMappedPort(4318);
        OtlpHttpMetricExporter exporter = OtlpHttpMetricExporter.builder()
                .setEndpoint(collectorBaseUrl + "/v1/metrics")
                .build();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.create(exporter))
                .build();
        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .build();

        Meter meter = openTelemetry.getMeter("testcontainers-example");
        DoubleGauge gauge = meter.gaugeBuilder("my.test.gauge").build();
        gauge.set(42.0);
        openTelemetry.shutdown();

        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            String body = restClient.get()
                    .uri("/api/metrics/list")
                    .retrieve()
                    .body(String.class);

            assertThat(body).isNotNull();
            assertThat(body).contains("my.test.gauge");
            assertThat(body).contains("42.0");
        });
    }
}