package com.github.siom79.opentelemetry.test.collector.adapters.otel.grpc;

import org.springframework.grpc.server.service.GrpcService;

import com.github.siom79.opentelemetry.test.collector.adapters.otel.TracesModelMapper;
import com.github.siom79.opentelemetry.test.collector.core.services.ProxyService;
import com.github.siom79.opentelemetry.test.collector.core.services.TracesService;

import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.trace.v1.ExportTracePartialSuccess;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GrpcService
public class GrpcTracesController extends TraceServiceGrpc.TraceServiceImplBase {

    private final TracesService tracesService;
    private final TracesModelMapper tracesModelMapper;
    private final ProxyService proxyService;

    public GrpcTracesController(TracesService tracesService,
                                TracesModelMapper tracesModelMapper,
                                ProxyService proxyService) {
        this.tracesService = tracesService;
        this.tracesModelMapper = tracesModelMapper;
        this.proxyService = proxyService;
    }

    @Override
    public void export(ExportTraceServiceRequest request, StreamObserver<ExportTraceServiceResponse> responseObserver) {
        log.debug("GRPC Trace Request: {}", request);
        tracesService.addResourceSpans(request.getResourceSpansList().stream().map(tracesModelMapper::mapResourceSpans).toList());
        proxyService.forwardTraces(request);
        ExportTraceServiceResponse response = ExportTraceServiceResponse.newBuilder()
                .setPartialSuccess(ExportTracePartialSuccess.newBuilder().build())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
