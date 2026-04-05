package com.github.siom79.opentelemetry.test.collector.core.model.metrics;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Gauge.class, name = "gauge"),
        @JsonSubTypes.Type(value = Sum.class, name = "sum"),
        @JsonSubTypes.Type(value = Histogram.class, name = "histogram"),
        @JsonSubTypes.Type(value = ExponentialHistogram.class, name = "exponential_histogram"),
        @JsonSubTypes.Type(value = Summary.class, name = "summary")
})
public class Data {
}
