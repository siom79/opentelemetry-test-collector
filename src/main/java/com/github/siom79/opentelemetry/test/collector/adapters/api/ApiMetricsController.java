package com.github.siom79.opentelemetry.test.collector.adapters.api;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.siom79.opentelemetry.test.collector.core.model.metrics.ResourceMetrics;
import com.github.siom79.opentelemetry.test.collector.core.services.MetricsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/metrics")
public class ApiMetricsController {

    private final MetricsService metricsService;

    public ApiMetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Operation(summary = "Returns a list of all captured metrics")
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Returns the list of metrics")
    )
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ResourceMetrics> list() {
        return metricsService.getMetrics();
    }

    @Operation(summary = "Clears the list of all captured metrics")
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "The metrics list has been cleared")
    )
    @PostMapping(value = "/clear")
    public void clear() {
        metricsService.clear();
    }
}
