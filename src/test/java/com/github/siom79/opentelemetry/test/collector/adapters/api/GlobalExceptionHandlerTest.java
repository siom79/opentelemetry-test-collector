package com.github.siom79.opentelemetry.test.collector.adapters.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.siom79.opentelemetry.test.collector.core.model.traces.OpentelemetryTestCollectorException;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/collector-exception")
        void throwCollectorException() {
            throw new OpentelemetryTestCollectorException("something went wrong");
        }

        @GetMapping("/test/unexpected-exception")
        void throwUnexpectedException() {
            throw new RuntimeException("unexpected");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void collectorException_returns400WithErrorIdAndMessage() throws Exception {
        mockMvc.perform(get("/test/collector-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorId").isNotEmpty())
                .andExpect(jsonPath("$.message").value("something went wrong"));
    }

    @Test
    void unexpectedException_returns500WithErrorIdAndGenericMessage() throws Exception {
        mockMvc.perform(get("/test/unexpected-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorId").isNotEmpty())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}