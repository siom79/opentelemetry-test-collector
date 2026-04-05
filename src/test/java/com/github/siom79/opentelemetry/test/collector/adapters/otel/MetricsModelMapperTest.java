package com.github.siom79.opentelemetry.test.collector.adapters.otel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.siom79.opentelemetry.test.collector.core.model.metrics.*;

class MetricsModelMapperTest {

    private MetricsModelMapper mapper;

    @BeforeEach
    void setUp() {
        CommonModelMapper commonModelMapper = new CommonModelMapper();
        ResourceModelMapper resourceModelMapper = new ResourceModelMapper(commonModelMapper);
        mapper = new MetricsModelMapper(resourceModelMapper, commonModelMapper);
    }

    @Test
    void map_gauge_withAsDouble_mapsDataPointCorrectly() {
        io.opentelemetry.proto.metrics.v1.NumberDataPoint ndp =
                io.opentelemetry.proto.metrics.v1.NumberDataPoint.newBuilder()
                        .setAsDouble(42.0)
                        .setStartTimeUnixNano(1000L)
                        .setTimeUnixNano(2000L)
                        .setFlags(1)
                        .build();
        io.opentelemetry.proto.metrics.v1.ResourceMetrics rm = resourceMetricsWithMetric(
                io.opentelemetry.proto.metrics.v1.Metric.newBuilder()
                        .setName("cpu.usage")
                        .setDescription("CPU usage")
                        .setUnit("%")
                        .setGauge(io.opentelemetry.proto.metrics.v1.Gauge.newBuilder()
                                .addDataPoints(ndp).build())
                        .build());

        ResourceMetrics result = mapper.map(rm);

        Metric mapped = result.getScopeMetrics().getFirst().getMetrics().getFirst();
        assertThat(mapped.getName()).isEqualTo("cpu.usage");
        assertThat(mapped.getDescription()).isEqualTo("CPU usage");
        assertThat(mapped.getUnit()).isEqualTo("%");
        assertThat(mapped.getData()).isInstanceOf(Gauge.class);

        Gauge gauge = (Gauge) mapped.getData();
        assertThat(gauge.getDataPoints()).hasSize(1);
        assertThat(gauge.getDataPoints().getFirst().getValue()).isEqualTo(42.0);
        assertThat(gauge.getDataPoints().getFirst().getStartTimeUnixNano()).isEqualTo(1000L);
        assertThat(gauge.getDataPoints().getFirst().getTimeUnixNano()).isEqualTo(2000L);
        assertThat(gauge.getDataPoints().getFirst().getFlags()).isEqualTo(1);
    }

    @Test
    void map_gauge_withAsInt_mapsValueCorrectly() {
        io.opentelemetry.proto.metrics.v1.NumberDataPoint ndp =
                io.opentelemetry.proto.metrics.v1.NumberDataPoint.newBuilder()
                        .setAsInt(100L)
                        .build();
        io.opentelemetry.proto.metrics.v1.ResourceMetrics rm = resourceMetricsWithMetric(
                io.opentelemetry.proto.metrics.v1.Metric.newBuilder()
                        .setName("request.count")
                        .setGauge(io.opentelemetry.proto.metrics.v1.Gauge.newBuilder()
                                .addDataPoints(ndp).build())
                        .build());

        ResourceMetrics result = mapper.map(rm);

        Gauge gauge = (Gauge) result.getScopeMetrics().getFirst().getMetrics().getFirst().getData();
        assertThat(gauge.getDataPoints().getFirst().getValue()).isEqualTo(100L);
    }

    @Test
    void map_sum_mapsAggregationTemporalityAndMonotonic() {
        io.opentelemetry.proto.metrics.v1.ResourceMetrics rm = resourceMetricsWithMetric(
                io.opentelemetry.proto.metrics.v1.Metric.newBuilder()
                        .setName("requests.total")
                        .setSum(io.opentelemetry.proto.metrics.v1.Sum.newBuilder()
                                .addDataPoints(io.opentelemetry.proto.metrics.v1.NumberDataPoint.newBuilder()
                                        .setAsDouble(5.0).build())
                                .setAggregationTemporality(
                                        io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE)
                                .setIsMonotonic(true)
                                .build())
                        .build());

        ResourceMetrics result = mapper.map(rm);

        Sum sum = (Sum) result.getScopeMetrics().getFirst().getMetrics().getFirst().getData();
        assertThat(sum.getAggregationTemporality())
                .isEqualTo(AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE);
        assertThat(sum.isMonotonic()).isTrue();
        assertThat(sum.getDataPoints()).hasSize(1);
        assertThat(sum.getDataPoints().getFirst().getValue()).isEqualTo(5.0);
    }

    @Test
    void map_histogram_mapsDataPointsAndBuckets() {
        io.opentelemetry.proto.metrics.v1.ResourceMetrics rm = resourceMetricsWithMetric(
                io.opentelemetry.proto.metrics.v1.Metric.newBuilder()
                        .setName("response.latency")
                        .setHistogram(io.opentelemetry.proto.metrics.v1.Histogram.newBuilder()
                                .addDataPoints(io.opentelemetry.proto.metrics.v1.HistogramDataPoint.newBuilder()
                                        .setCount(10)
                                        .setSum(55.0)
                                        .addBucketCounts(3)
                                        .addBucketCounts(7)
                                        .addExplicitBounds(5.0)
                                        .setStartTimeUnixNano(1000L)
                                        .setTimeUnixNano(2000L)
                                        .build())
                                .setAggregationTemporality(
                                        io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA)
                                .build())
                        .build());

        ResourceMetrics result = mapper.map(rm);

        Histogram histogram = (Histogram) result.getScopeMetrics().getFirst().getMetrics().getFirst().getData();
        assertThat(histogram.getAggregationTemporality())
                .isEqualTo(AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA);
        assertThat(histogram.getDataPoints()).hasSize(1);

        HistogramDataPoint dp = histogram.getDataPoints().getFirst();
        assertThat(dp.getCount()).isEqualTo(10);
        assertThat(dp.getSum()).isEqualTo(55.0);
        assertThat(dp.getBucketCounts()).containsExactly(3L, 7L);
        assertThat(dp.getExplicitBounds()).containsExactly(5.0);
    }

    @Test
    void map_summary_mapsQuantileValues() {
        io.opentelemetry.proto.metrics.v1.ResourceMetrics rm = resourceMetricsWithMetric(
                io.opentelemetry.proto.metrics.v1.Metric.newBuilder()
                        .setName("response.time.summary")
                        .setSummary(io.opentelemetry.proto.metrics.v1.Summary.newBuilder()
                                .addDataPoints(io.opentelemetry.proto.metrics.v1.SummaryDataPoint.newBuilder()
                                        .setCount(100)
                                        .setSum(5000.0)
                                        .addQuantileValues(
                                                io.opentelemetry.proto.metrics.v1.SummaryDataPoint.ValueAtQuantile.newBuilder()
                                                        .setQuantile(0.99)
                                                        .setValue(120.0)
                                                        .build())
                                        .build())
                                .build())
                        .build());

        ResourceMetrics result = mapper.map(rm);

        Summary summary = (Summary) result.getScopeMetrics().getFirst().getMetrics().getFirst().getData();
        assertThat(summary.getDataPoints()).hasSize(1);

        SummaryDataPoint dp = summary.getDataPoints().getFirst();
        assertThat(dp.getCount()).isEqualTo(100);
        assertThat(dp.getSum()).isEqualTo(5000.0);
        assertThat(dp.getQuantileValues()).hasSize(1);
        assertThat(dp.getQuantileValues().getFirst().getQuantile()).isEqualTo(0.99);
        assertThat(dp.getQuantileValues().getFirst().getValue()).isEqualTo(120.0);
    }

    @Test
    void map_exponentialHistogram_mapsBuckets() {
        io.opentelemetry.proto.metrics.v1.ResourceMetrics rm = resourceMetricsWithMetric(
                io.opentelemetry.proto.metrics.v1.Metric.newBuilder()
                        .setName("exp.histogram")
                        .setExponentialHistogram(io.opentelemetry.proto.metrics.v1.ExponentialHistogram.newBuilder()
                                .addDataPoints(io.opentelemetry.proto.metrics.v1.ExponentialHistogramDataPoint.newBuilder()
                                        .setCount(50)
                                        .setScale(2)
                                        .setZeroCount(5)
                                        .setPositive(io.opentelemetry.proto.metrics.v1.ExponentialHistogramDataPoint.Buckets.newBuilder()
                                                .setOffset(1)
                                                .addBucketCounts(10)
                                                .addBucketCounts(20)
                                                .build())
                                        .setNegative(io.opentelemetry.proto.metrics.v1.ExponentialHistogramDataPoint.Buckets.newBuilder()
                                                .setOffset(0)
                                                .build())
                                        .build())
                                .setAggregationTemporality(
                                        io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE)
                                .build())
                        .build());

        ResourceMetrics result = mapper.map(rm);

        ExponentialHistogram expHist =
                (ExponentialHistogram) result.getScopeMetrics().getFirst().getMetrics().getFirst().getData();
        assertThat(expHist.getDataPoints()).hasSize(1);

        ExponentialHistogramDataPoint dp = expHist.getDataPoints().getFirst();
        assertThat(dp.getCount()).isEqualTo(50);
        assertThat(dp.getScale()).isEqualTo(2);
        assertThat(dp.getZeroCount()).isEqualTo(5);
        assertThat(dp.getPositive().getOffset()).isEqualTo(1);
        assertThat(dp.getPositive().getBucketCounts()).containsExactly(10L, 20L);
    }

    @Test
    void map_noDataSet_returnsNullData() {
        io.opentelemetry.proto.metrics.v1.ResourceMetrics rm = resourceMetricsWithMetric(
                io.opentelemetry.proto.metrics.v1.Metric.newBuilder()
                        .setName("empty.metric")
                        .build());

        ResourceMetrics result = mapper.map(rm);

        assertThat(result.getScopeMetrics().getFirst().getMetrics().getFirst().getData()).isNull();
    }

    @Test
    void map_scopeMetrics_mapsInstrumentationScope() {
        io.opentelemetry.proto.metrics.v1.ResourceMetrics rm =
                io.opentelemetry.proto.metrics.v1.ResourceMetrics.newBuilder()
                        .addScopeMetrics(io.opentelemetry.proto.metrics.v1.ScopeMetrics.newBuilder()
                                .setScope(io.opentelemetry.proto.common.v1.InstrumentationScope.newBuilder()
                                        .setName("my-library")
                                        .setVersion("1.2.3")
                                        .build())
                                .setSchemaUrl("https://example.com/schema")
                                .build())
                        .build();

        ResourceMetrics result = mapper.map(rm);

        assertThat(result.getScopeMetrics().getFirst().getInstrumentationScope().getName()).isEqualTo("my-library");
        assertThat(result.getScopeMetrics().getFirst().getInstrumentationScope().getVersion()).isEqualTo("1.2.3");
        assertThat(result.getScopeMetrics().getFirst().getSchemaUrl()).isEqualTo("https://example.com/schema");
    }

    @Test
    void map_exemplar_withAsDouble_mapsValue() {
        io.opentelemetry.proto.metrics.v1.NumberDataPoint ndp =
                io.opentelemetry.proto.metrics.v1.NumberDataPoint.newBuilder()
                        .setAsDouble(1.0)
                        .addExemplars(io.opentelemetry.proto.metrics.v1.Exemplar.newBuilder()
                                .setAsDouble(3.14)
                                .setTimeUnixNano(500L)
                                .build())
                        .build();
        io.opentelemetry.proto.metrics.v1.ResourceMetrics rm = resourceMetricsWithMetric(
                io.opentelemetry.proto.metrics.v1.Metric.newBuilder()
                        .setName("metric.with.exemplar")
                        .setGauge(io.opentelemetry.proto.metrics.v1.Gauge.newBuilder()
                                .addDataPoints(ndp).build())
                        .build());

        ResourceMetrics result = mapper.map(rm);

        Gauge gauge = (Gauge) result.getScopeMetrics().getFirst().getMetrics().getFirst().getData();
        List<Exemplar> exemplars = gauge.getDataPoints().getFirst().getExemplars();
        assertThat(exemplars).hasSize(1);
        assertThat(exemplars.getFirst().getValue()).isEqualTo(3.14);
        assertThat(exemplars.getFirst().getTimeUnixNano()).isEqualTo(500L);
    }

    @Test
    void map_exemplar_withAsInt_mapsValue() {
        io.opentelemetry.proto.metrics.v1.NumberDataPoint ndp =
                io.opentelemetry.proto.metrics.v1.NumberDataPoint.newBuilder()
                        .setAsDouble(1.0)
                        .addExemplars(io.opentelemetry.proto.metrics.v1.Exemplar.newBuilder()
                                .setAsInt(7L)
                                .build())
                        .build();
        io.opentelemetry.proto.metrics.v1.ResourceMetrics rm = resourceMetricsWithMetric(
                io.opentelemetry.proto.metrics.v1.Metric.newBuilder()
                        .setName("metric.with.exemplar.int")
                        .setGauge(io.opentelemetry.proto.metrics.v1.Gauge.newBuilder()
                                .addDataPoints(ndp).build())
                        .build());

        ResourceMetrics result = mapper.map(rm);

        Gauge gauge = (Gauge) result.getScopeMetrics().getFirst().getMetrics().getFirst().getData();
        assertThat(gauge.getDataPoints().getFirst().getExemplars().getFirst().getValue()).isEqualTo(7L);
    }

    // --- helper ---

    private io.opentelemetry.proto.metrics.v1.ResourceMetrics resourceMetricsWithMetric(
            io.opentelemetry.proto.metrics.v1.Metric metric) {
        return io.opentelemetry.proto.metrics.v1.ResourceMetrics.newBuilder()
                .addScopeMetrics(io.opentelemetry.proto.metrics.v1.ScopeMetrics.newBuilder()
                        .setScope(io.opentelemetry.proto.common.v1.InstrumentationScope.newBuilder()
                                .setName("test-scope").build())
                        .addMetrics(metric)
                        .build())
                .build();
    }
}