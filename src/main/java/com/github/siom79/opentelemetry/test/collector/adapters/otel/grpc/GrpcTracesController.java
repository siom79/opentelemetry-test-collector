package com.github.siom79.opentelemetry.test.collector.adapters.otel.grpc;

import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.trace.v1.ExportTracePartialSuccess;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;

@Slf4j
@GrpcService
public class GrpcTracesController extends TraceServiceGrpc.TraceServiceImplBase {
    @Override
    public void export(ExportTraceServiceRequest request, StreamObserver<ExportTraceServiceResponse> responseObserver) {
        log.info("GRPC Trace Request: {}", request);
        List<ResourceSpans> resourceSpansList = request.getResourceSpansList();
        for (ResourceSpans spans : resourceSpansList) {
            log.info("spans: {}", spans);
            String schemaUrl = spans.getSchemaUrl();
            log.info("schemaUrl: {}", schemaUrl);
            Resource resource = spans.getResource();
            List<KeyValue> attributesList = resource.getAttributesList();
            for (KeyValue keyValue : attributesList) {
                log.info("keyValue: {}", keyValue);
            }
        }
        ExportTraceServiceResponse response = ExportTraceServiceResponse.newBuilder()
                .setPartialSuccess(ExportTracePartialSuccess.newBuilder().build())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
