package com.github.siom79.opentelemetry.test.collector.core.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.siom79.opentelemetry.test.collector.core.model.logs.ResourceLogs;

class LogsServiceTest {

    private LogsService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new LogsService();
        Field cacheSizeField = LogsService.class.getDeclaredField("cacheSize");
        cacheSizeField.setAccessible(true);
        cacheSizeField.set(service, 3);
        service.postConstruct();
    }

    @Test
    void addResourceLogs_andGet_returnsAddedEntries() {
        service.addResourceLogs(List.of(
                ResourceLogs.builder().schemaUrl("https://schema.a").build(),
                ResourceLogs.builder().schemaUrl("https://schema.b").build()
        ));

        List<ResourceLogs> result = service.getResourceLogs();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSchemaUrl()).isEqualTo("https://schema.a");
        assertThat(result.get(1).getSchemaUrl()).isEqualTo("https://schema.b");
    }

    @Test
    void clear_removesAllEntries() {
        service.addResourceLogs(List.of(ResourceLogs.builder().build()));
        service.clear();

        assertThat(service.getResourceLogs()).isEmpty();
    }

    @Test
    void getResourceLogs_returnsEmptyList_whenNothingAdded() {
        assertThat(service.getResourceLogs()).isEmpty();
    }

    @Test
    void cacheSize_evictsOldestEntry_whenFull() {
        for (int i = 0; i < 4; i++) {
            service.addResourceLogs(List.of(ResourceLogs.builder().schemaUrl("url" + i).build()));
        }

        List<ResourceLogs> result = service.getResourceLogs();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getSchemaUrl()).isEqualTo("url1");
        assertThat(result.get(1).getSchemaUrl()).isEqualTo("url2");
        assertThat(result.get(2).getSchemaUrl()).isEqualTo("url3");
    }

    @Test
    void addResourceLogs_multipleBatches_accumulatesEntries() {
        service.addResourceLogs(List.of(ResourceLogs.builder().schemaUrl("first").build()));
        service.addResourceLogs(List.of(ResourceLogs.builder().schemaUrl("second").build()));

        assertThat(service.getResourceLogs()).hasSize(2);
    }
}
