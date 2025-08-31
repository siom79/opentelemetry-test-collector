package com.github.siom79.opentelemetry.test.collector.adapters.otel.http;

import com.github.siom79.opentelemetry.test.collector.adapters.otel.TracesModelMapper;
import com.github.siom79.opentelemetry.test.collector.core.services.TracesService;
import io.opentelemetry.proto.collector.trace.v1.ExportTracePartialSuccess;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@Slf4j
@RestController
public class HttpTracesController {

    private final TracesService tracesService;
    private final TracesModelMapper tracesModelMapper;

    public HttpTracesController(TracesService tracesService,
                                TracesModelMapper tracesModelMapper) {
        this.tracesService = tracesService;
        this.tracesModelMapper = tracesModelMapper;
    }

    @PostMapping(
            value = "/v1/traces",
            consumes = "application/x-protobuf",
            produces = "application/x-protobuf"
    )
    public ExportTraceServiceResponse exportTraces(@RequestBody ExportTraceServiceRequest request) {
        log.info("HTTP Traces request: {}", request);
        tracesService.addResourceSpans(request.getResourceSpansList().stream().map(tracesModelMapper::mapResourceSpans).toList());
        return ExportTraceServiceResponse.newBuilder()
                .setPartialSuccess(ExportTracePartialSuccess.newBuilder()
                        .build())
                .build();
    }
}
