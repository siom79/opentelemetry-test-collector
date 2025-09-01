package com.github.siom79.opentelemetry.test.collector.adapters.otel.grpc;

import com.github.siom79.opentelemetry.test.collector.adapters.otel.MetricsModelMapper;
import com.github.siom79.opentelemetry.test.collector.core.services.MetricsService;
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

    private final MetricsService metricsService;
    private final MetricsModelMapper modelMapper;

    public GrpcMetricsController(MetricsService metricsService,
                                 MetricsModelMapper modelMapper) {
        this.metricsService = metricsService;
        this.modelMapper = modelMapper;
    }

    @Override
    public void export(ExportMetricsServiceRequest request, StreamObserver<ExportMetricsServiceResponse> responseObserver) {
        log.info("GRPC Metrics Request: {}", request);
        metricsService.addMetrics(request.getResourceMetricsList().stream().map(modelMapper::map).toList());
        responseObserver.onNext(ExportMetricsServiceResponse.newBuilder().setPartialSuccess(ExportMetricsPartialSuccess.newBuilder().build()).build());
        responseObserver.onCompleted();
    }
}
