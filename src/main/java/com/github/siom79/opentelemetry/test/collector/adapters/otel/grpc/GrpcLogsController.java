package com.github.siom79.opentelemetry.test.collector.adapters.otel.grpc;

import org.springframework.grpc.server.service.GrpcService;

import com.github.siom79.opentelemetry.test.collector.adapters.otel.LogsModelMapper;
import com.github.siom79.opentelemetry.test.collector.core.services.LogsService;
import com.github.siom79.opentelemetry.test.collector.core.services.ProxyService;

import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsPartialSuccess;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GrpcService
public class GrpcLogsController extends LogsServiceGrpc.LogsServiceImplBase {

    private final LogsService logsService;
    private final LogsModelMapper logsModelMapper;
    private final ProxyService proxyService;

    public GrpcLogsController(LogsService logsService,
                              LogsModelMapper logsModelMapper,
                              ProxyService proxyService) {
        this.logsService = logsService;
        this.logsModelMapper = logsModelMapper;
        this.proxyService = proxyService;
    }

    @Override
    public void export(ExportLogsServiceRequest request, StreamObserver<ExportLogsServiceResponse> responseObserver) {
        log.debug("GRPC Logs Request: {}", request);
        logsService.addResourceLogs(request.getResourceLogsList().stream().map(logsModelMapper::mapResourceLogs).toList());
        proxyService.forwardLogs(request);
        ExportLogsServiceResponse response = ExportLogsServiceResponse.newBuilder()
                .setPartialSuccess(ExportLogsPartialSuccess.newBuilder().build())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
