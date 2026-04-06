package com.github.siom79.opentelemetry.test.collector.core.services;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

@Slf4j
@Service
public class ProxyService implements DisposableBean {

    @Value("${com.github.siom79.opentelemetry-test-collector.proxy.endpoint:}")
    private String endpoint;

    private enum Protocol { HTTP, GRPC }

    private Protocol protocol;
    private String httpBaseUrl;
    private RestTemplate restTemplate;
    private ManagedChannel grpcChannel;
    private TraceServiceGrpc.TraceServiceBlockingStub traceStub;
    private MetricsServiceGrpc.MetricsServiceBlockingStub metricsStub;

    @PostConstruct
    void init() {
        if (endpoint == null || endpoint.isBlank()) {
            log.info("Proxy is disabled (PROXY_ENDPOINT not set).");
            return;
        }

        if (endpoint.startsWith("grpc://")) {
            protocol = Protocol.GRPC;
            URI uri = URI.create(endpoint);
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 4317;
            grpcChannel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
            traceStub = TraceServiceGrpc.newBlockingStub(grpcChannel);
            metricsStub = MetricsServiceGrpc.newBlockingStub(grpcChannel);
            log.info("Proxy enabled (gRPC): {}:{}", host, port);
        } else {
            protocol = Protocol.HTTP;
            httpBaseUrl = endpoint.replaceAll("/+$", "");
            restTemplate = new RestTemplate();
            log.info("Proxy enabled (HTTP): {}", httpBaseUrl);
        }
    }

    public boolean isEnabled() {
        return protocol != null;
    }

    public void forwardTraces(ExportTraceServiceRequest request) {
        if (!isEnabled()) return;
        try {
            if (protocol == Protocol.HTTP) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("application/x-protobuf"));
                headers.setAccept(List.of(MediaType.parseMediaType("application/x-protobuf")));
                HttpEntity<byte[]> entity = new HttpEntity<>(request.toByteArray(), headers);
                restTemplate.postForObject(httpBaseUrl + "/v1/traces", entity, byte[].class);
            } else {
                traceStub.export(request);
            }
            log.debug("Proxy: forwarded traces to {}", endpoint);
        } catch (Exception e) {
            log.warn("Proxy: failed to forward traces to {}: {}", endpoint, e.getMessage());
        }
    }

    public void forwardMetrics(ExportMetricsServiceRequest request) {
        if (!isEnabled()) return;
        try {
            if (protocol == Protocol.HTTP) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("application/x-protobuf"));
                headers.setAccept(List.of(MediaType.parseMediaType("application/x-protobuf")));
                HttpEntity<byte[]> entity = new HttpEntity<>(request.toByteArray(), headers);
                restTemplate.postForObject(httpBaseUrl + "/v1/metrics", entity, byte[].class);
            } else {
                metricsStub.export(request);
            }
            log.debug("Proxy: forwarded metrics to {}", endpoint);
        } catch (Exception e) {
            log.warn("Proxy: failed to forward metrics to {}: {}", endpoint, e.getMessage());
        }
    }

    @Override
    public void destroy() {
        if (grpcChannel != null && !grpcChannel.isShutdown()) {
            grpcChannel.shutdown();
            log.info("Proxy gRPC channel shut down.");
        }
    }
}
