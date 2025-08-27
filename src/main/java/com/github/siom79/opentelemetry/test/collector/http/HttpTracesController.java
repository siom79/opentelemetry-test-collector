package com.github.siom79.opentelemetry.test.collector.http;

import io.opentelemetry.proto.collector.trace.v1.ExportTracePartialSuccess;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HttpTracesController {

    @PostMapping(
            value = "/v1/traces",
            consumes = "application/x-protobuf",
            produces = "application/x-protobuf"
    )
    public ExportTraceServiceResponse exportTraces(@RequestBody ExportTraceServiceRequest request) {
        log.info("HTTP Traces request: {}", request);
        return ExportTraceServiceResponse.newBuilder()
                .setPartialSuccess(ExportTracePartialSuccess.newBuilder()
                        .build())
                .build();
    }
}
