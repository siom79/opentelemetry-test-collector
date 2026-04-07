package com.github.siom79.opentelemetry.test.collector.core.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.siom79.opentelemetry.test.collector.core.model.metrics.ResourceMetrics;

class MetricsServiceTest {

    private MetricsService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new MetricsService();
        Field cacheSizeField = MetricsService.class.getDeclaredField("cacheSize");
        cacheSizeField.setAccessible(true);
        cacheSizeField.set(service, 3);
        service.postConstruct();
    }

    @Test
    void addMetrics_andGet_returnsAddedEntries() {
        service.addMetrics(List.of(
                ResourceMetrics.builder().schemaUrl("https://schema.a").build(),
                ResourceMetrics.builder().schemaUrl("https://schema.b").build()
        ));

        List<ResourceMetrics> result = service.getMetrics();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSchemaUrl()).isEqualTo("https://schema.a");
        assertThat(result.get(1).getSchemaUrl()).isEqualTo("https://schema.b");
    }

    @Test
    void clear_removesAllEntries() {
        service.addMetrics(List.of(ResourceMetrics.builder().build()));
        service.clear();

        assertThat(service.getMetrics()).isEmpty();
    }

    @Test
    void getMetrics_returnsEmptyList_whenNothingAdded() {
        assertThat(service.getMetrics()).isEmpty();
    }

    @Test
    void cacheSize_evictsOldestEntry_whenFull() {
        for (int i = 0; i < 4; i++) {
            service.addMetrics(List.of(ResourceMetrics.builder().schemaUrl("url" + i).build()));
        }

        List<ResourceMetrics> result = service.getMetrics();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getSchemaUrl()).isEqualTo("url1");
        assertThat(result.get(1).getSchemaUrl()).isEqualTo("url2");
        assertThat(result.get(2).getSchemaUrl()).isEqualTo("url3");
    }

    @Test
    void addMetrics_multipleBatches_accumulatesEntries() {
        service.addMetrics(List.of(ResourceMetrics.builder().schemaUrl("first").build()));
        service.addMetrics(List.of(ResourceMetrics.builder().schemaUrl("second").build()));

        assertThat(service.getMetrics()).hasSize(2);
    }

    @Test
    void getMetrics_returnsSnapshot_notLiveView() {
        service.addMetrics(List.of(ResourceMetrics.builder().schemaUrl("before").build()));
        List<ResourceMetrics> snapshot = service.getMetrics();

        service.addMetrics(List.of(ResourceMetrics.builder().schemaUrl("after").build()));

        assertThat(snapshot).hasSize(1);
        assertThat(service.getMetrics()).hasSize(2);
    }
}
