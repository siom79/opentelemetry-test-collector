package com.github.siom79.opentelemetry.test.collector;

import com.github.siom79.opentelemetry.test.collector.core.services.MetricsService;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class HttpMetricsIT {

    private OpenTelemetrySdk openTelemetry;
    @Autowired
    private MetricsService metricsService;

    @BeforeEach
    void beforeEach() {
        OtlpHttpMetricExporter exporter = OtlpHttpMetricExporter.builder()
                .setEndpoint("http://localhost:4318/v1/metrics")
                .build();
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.create(exporter))
                .build();
        openTelemetry = OpenTelemetrySdk.builder()
                .setMeterProvider(meterProvider)
                .build();
    }

    @Test
    void test() {
        Meter meter = openTelemetry.getMeter("example-app");
        String cpu = "cpu";
        DoubleGauge gauge = meter.gaugeBuilder(cpu).build();
        gauge.set(42.0);
        openTelemetry.shutdown();

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> !metricsService.getMetrics().isEmpty());
        Assertions.assertThat(metricsService.getMetrics()).isNotEmpty();
        Assertions.assertThat(metricsService.getMetrics().getFirst().getScopeMetrics().getFirst().getMetrics().getFirst().getName()).isEqualTo(cpu);
    }
}
