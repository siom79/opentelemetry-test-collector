package com.github.siom79.opentelemetry.test.collector.adapters.otel.http;

import com.github.siom79.opentelemetry.test.collector.adapters.otel.MetricsModelMapper;
import com.github.siom79.opentelemetry.test.collector.core.services.MetricsService;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsPartialSuccess;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@Slf4j
@RestController
public class HttpMetricsController {

    private final MetricsService metricsService;
    private final MetricsModelMapper modelMapper;

    public HttpMetricsController(MetricsService metricsService,
                                 MetricsModelMapper modelMapper) {
        this.metricsService = metricsService;
        this.modelMapper = modelMapper;
    }

    @PostMapping(
            value = "/v1/metrics",
            consumes = "application/x-protobuf",
            produces = "application/x-protobuf"
    )
    public ExportMetricsServiceResponse exportMetrics(@RequestBody ExportMetricsServiceRequest request) {
        log.info("HTTP Metrics request: {}", request);
        metricsService.addMetrics(request.getResourceMetricsList().stream().map(modelMapper::map).toList());
        return ExportMetricsServiceResponse.newBuilder()
                .setPartialSuccess(ExportMetricsPartialSuccess.newBuilder()
                        .build())
                .build();
    }
}
