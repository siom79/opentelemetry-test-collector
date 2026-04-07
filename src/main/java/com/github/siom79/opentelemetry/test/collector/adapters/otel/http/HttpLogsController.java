package com.github.siom79.opentelemetry.test.collector.adapters.otel.http;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.siom79.opentelemetry.test.collector.adapters.otel.LogsModelMapper;
import com.github.siom79.opentelemetry.test.collector.core.services.LogsService;
import com.github.siom79.opentelemetry.test.collector.core.services.ProxyService;

import io.opentelemetry.proto.collector.logs.v1.ExportLogsPartialSuccess;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;

@Hidden
@Slf4j
@RestController
public class HttpLogsController {

    private final LogsService logsService;
    private final LogsModelMapper logsModelMapper;
    private final ProxyService proxyService;

    public HttpLogsController(LogsService logsService,
                              LogsModelMapper logsModelMapper,
                              ProxyService proxyService) {
        this.logsService = logsService;
        this.logsModelMapper = logsModelMapper;
        this.proxyService = proxyService;
    }

    @PostMapping(
            value = "/v1/logs",
            consumes = "application/x-protobuf",
            produces = "application/x-protobuf"
    )
    public ExportLogsServiceResponse exportLogs(@RequestBody ExportLogsServiceRequest request) {
        log.debug("HTTP Logs request: {}", request);
        logsService.addResourceLogs(request.getResourceLogsList().stream().map(logsModelMapper::mapResourceLogs).toList());
        proxyService.forwardLogs(request);
        return ExportLogsServiceResponse.newBuilder()
                .setPartialSuccess(ExportLogsPartialSuccess.newBuilder()
                        .build())
                .build();
    }
}
