package com.github.siom79.opentelemetry.test.collector.adapters.api;

import com.github.siom79.opentelemetry.test.collector.core.model.ResourceSpans;
import com.github.siom79.opentelemetry.test.collector.core.services.TracesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/traces")
public class ApiTracesController {

    private final TracesService tracesService;

    public ApiTracesController(TracesService tracesService) {
        this.tracesService = tracesService;
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ResourceSpans> list() {
        return tracesService.getResourceSpans();
    }

    @PostMapping(value = "/clear")
    public void clear() {
        tracesService.clear();
    }
}
