package com.github.siom79.opentelemetry.test.collector.grpc;

import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsPartialSuccess;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class GrpcMetricsController extends MetricsServiceGrpc.MetricsServiceImplBase {
    @Override
    public void export(ExportMetricsServiceRequest request, StreamObserver<ExportMetricsServiceResponse> responseObserver) {
        log.info("GRPC Metrics Request: {}", request);
        responseObserver.onNext(ExportMetricsServiceResponse.newBuilder().setPartialSuccess(ExportMetricsPartialSuccess.newBuilder().build()).build());
        responseObserver.onCompleted();
    }
}
