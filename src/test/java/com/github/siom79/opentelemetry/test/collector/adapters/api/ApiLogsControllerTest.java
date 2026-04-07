package com.github.siom79.opentelemetry.test.collector.adapters.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.github.siom79.opentelemetry.test.collector.core.model.logs.ResourceLogs;
import com.github.siom79.opentelemetry.test.collector.core.services.LogsService;

class ApiLogsControllerTest {

    private MockMvc mockMvc;
    private LogsService logsService;

    @BeforeEach
    void setUp() {
        logsService = Mockito.mock(LogsService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ApiLogsController(logsService)).build();
    }

    @Test
    void list_returnsLogsFromService() throws Exception {
        when(logsService.getResourceLogs()).thenReturn(List.of(
                ResourceLogs.builder().schemaUrl("https://schema.test").build()
        ));

        mockMvc.perform(get("/api/logs/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].schemaUrl").value("https://schema.test"));

        verify(logsService).getResourceLogs();
    }

    @Test
    void list_returnsEmptyArray_whenNoLogs() throws Exception {
        when(logsService.getResourceLogs()).thenReturn(List.of());

        mockMvc.perform(get("/api/logs/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(logsService).getResourceLogs();
    }

    @Test
    void clear_callsServiceClear_andReturns200() throws Exception {
        mockMvc.perform(post("/api/logs/clear"))
                .andExpect(status().isOk());

        verify(logsService).clear();
    }
}
