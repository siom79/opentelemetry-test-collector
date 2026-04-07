package com.github.siom79.opentelemetry.test.collector.adapters.api;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.siom79.opentelemetry.test.collector.core.model.logs.ResourceLogs;
import com.github.siom79.opentelemetry.test.collector.core.services.LogsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/logs")
public class ApiLogsController {

    private final LogsService logsService;

    public ApiLogsController(LogsService logsService) {
        this.logsService = logsService;
    }

    @Operation(summary = "Returns a list of all captured logs")
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "Returns the list of logs")
    )
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ResourceLogs> list() {
        return logsService.getResourceLogs();
    }

    @Operation(summary = "Clears the list of all captured logs")
    @ApiResponses(
            @ApiResponse(responseCode = "200", description = "The list has been cleared")
    )
    @PostMapping(value = "/clear")
    public void clear() {
        logsService.clear();
    }
}
