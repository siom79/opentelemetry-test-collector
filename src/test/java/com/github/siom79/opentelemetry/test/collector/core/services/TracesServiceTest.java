package com.github.siom79.opentelemetry.test.collector.core.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.siom79.opentelemetry.test.collector.core.model.traces.ResourceSpans;

class TracesServiceTest {

    private TracesService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new TracesService();
        Field cacheSizeField = TracesService.class.getDeclaredField("cacheSize");
        cacheSizeField.setAccessible(true);
        cacheSizeField.set(service, 3);
        service.postConstruct();
    }

    @Test
    void addResourceSpans_andGet_returnsAddedEntries() {
        service.addResourceSpans(List.of(
                ResourceSpans.builder().schemaUrl("https://schema.a").build(),
                ResourceSpans.builder().schemaUrl("https://schema.b").build()
        ));

        List<ResourceSpans> result = service.getResourceSpans();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSchemaUrl()).isEqualTo("https://schema.a");
        assertThat(result.get(1).getSchemaUrl()).isEqualTo("https://schema.b");
    }

    @Test
    void clear_removesAllEntries() {
        service.addResourceSpans(List.of(ResourceSpans.builder().build()));
        service.clear();

        assertThat(service.getResourceSpans()).isEmpty();
    }

    @Test
    void getResourceSpans_returnsEmptyList_whenNothingAdded() {
        assertThat(service.getResourceSpans()).isEmpty();
    }

    @Test
    void cacheSize_evictsOldestEntry_whenFull() {
        for (int i = 0; i < 4; i++) {
            service.addResourceSpans(List.of(ResourceSpans.builder().schemaUrl("url" + i).build()));
        }

        List<ResourceSpans> result = service.getResourceSpans();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getSchemaUrl()).isEqualTo("url1");
        assertThat(result.get(1).getSchemaUrl()).isEqualTo("url2");
        assertThat(result.get(2).getSchemaUrl()).isEqualTo("url3");
    }

    @Test
    void addResourceSpans_multipleBatches_accumulatesEntries() {
        service.addResourceSpans(List.of(ResourceSpans.builder().schemaUrl("first").build()));
        service.addResourceSpans(List.of(ResourceSpans.builder().schemaUrl("second").build()));

        assertThat(service.getResourceSpans()).hasSize(2);
    }

    @Test
    void getResourceSpans_returnsSnapshot_notLiveView() {
        service.addResourceSpans(List.of(ResourceSpans.builder().schemaUrl("before").build()));
        List<ResourceSpans> snapshot = service.getResourceSpans();

        service.addResourceSpans(List.of(ResourceSpans.builder().schemaUrl("after").build()));

        assertThat(snapshot).hasSize(1);
        assertThat(service.getResourceSpans()).hasSize(2);
    }
}
