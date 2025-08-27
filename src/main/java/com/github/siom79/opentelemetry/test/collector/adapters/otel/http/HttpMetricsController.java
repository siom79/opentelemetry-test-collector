package com.github.siom79.opentelemetry.test.collector.adapters.otel.http;

import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsPartialSuccess;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HttpMetricsController {

    @PostMapping(
            value = "/v1/metrics",
            consumes = "application/x-protobuf",
            produces = "application/x-protobuf"
    )
    public ExportMetricsServiceResponse exportMetrics(@RequestBody ExportMetricsServiceRequest request) {
        log.info("HTTP Metrics request: {}", request);
        return ExportMetricsServiceResponse.newBuilder()
                .setPartialSuccess(ExportMetricsPartialSuccess.newBuilder()
                        .build())
                .build();
    }
}
