package com.github.siom79.opentelemetry.test.collector.adapters.otel;

import com.github.siom79.opentelemetry.test.collector.core.model.metrics.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.github.siom79.opentelemetry.test.collector.core.util.HexUtils.bytesToHex;

@Slf4j
@Service
public class MetricsModelMapper {

    private final ResourceModelMapper resourceModelMapper;
    private final CommonModelMapper commonModelMapper;

    public MetricsModelMapper(ResourceModelMapper resourceModelMapper,
                              CommonModelMapper commonModelMapper) {
        this.resourceModelMapper = resourceModelMapper;
        this.commonModelMapper = commonModelMapper;
    }

    public ResourceMetrics map(io.opentelemetry.proto.metrics.v1.ResourceMetrics rm) {
        return ResourceMetrics.builder()
                .resource(resourceModelMapper.mapResource(rm.getResource()))
                .scopeMetrics(mapScopeMetrics(rm.getScopeMetricsList()))
                .schemaUrl(rm.getSchemaUrl())
                .build();
    }

    private List<ScopeMetrics> mapScopeMetrics(List<io.opentelemetry.proto.metrics.v1.ScopeMetrics> scopeMetricsList) {
        return scopeMetricsList.stream().map(this::mapScopeMetric).toList();
    }

    private ScopeMetrics mapScopeMetric(io.opentelemetry.proto.metrics.v1.ScopeMetrics sm) {
        return ScopeMetrics.builder()
                .instrumentationScope(this.commonModelMapper.mapInstrumentationScope(sm.getScope()))
                .metrics(mapMetrics(sm.getMetricsList()))
                .schemaUrl(sm.getSchemaUrl())
                .build();
    }

    private List<Metric> mapMetrics(List<io.opentelemetry.proto.metrics.v1.Metric> metricsList) {
        return metricsList.stream().map(this::mapMetric).toList();
    }

    private Metric mapMetric(io.opentelemetry.proto.metrics.v1.Metric m) {
        return Metric.builder()
                .name(m.getName())
                .description(m.getDescription())
                .unit(m.getUnit())
                .data(mapData(m))
                .metadata(this.commonModelMapper.mapKeyValueList(m.getMetadataList()))
                .build();
    }

    private Data mapData(io.opentelemetry.proto.metrics.v1.Metric metric) {
        switch (metric.getDataCase()) {
            case GAUGE -> {
                return Gauge.builder()
                        .dataPoints(mapDataPoints(metric.getGauge().getDataPointsList()))
                        .build();
            }
            case SUM -> {
                return Sum.builder()
                        .dataPoints(mapDataPoints(metric.getSum().getDataPointsList()))
                        .aggregationTemporality(mapAggregationTemporality(metric.getSum().getAggregationTemporality()))
                        .isMonotonic(metric.getSum().getIsMonotonic())
                        .build();
            }
            case HISTOGRAM -> {
                return Histogram.builder()
                        .dataPoints(mapHistogramDataPoints(metric.getHistogram().getDataPointsList()))
                        .aggregationTemporality(mapAggregationTemporality(metric.getHistogram().getAggregationTemporality()))
                        .build();
            }
            case EXPONENTIAL_HISTOGRAM -> {
                return ExponentialHistogram.builder()
                        .dataPoints(mapExponentialHistogramDataPoints(metric.getExponentialHistogram().getDataPointsList()))
                        .aggregationTemporality(mapAggregationTemporality(metric.getExponentialHistogram().getAggregationTemporality()))
                        .build();
            }
            case SUMMARY -> {
                return Summary.builder()
                        .dataPoints(mapSummaryDataPoints(metric.getSummary().getDataPointsList()))
                        .build();
            }
        }
        return null;
    }

    private AggregationTemporality mapAggregationTemporality(io.opentelemetry.proto.metrics.v1.AggregationTemporality aggregationTemporality) {
        switch (aggregationTemporality) {
            case AGGREGATION_TEMPORALITY_UNSPECIFIED -> {
                return AggregationTemporality.AGGREGATION_TEMPORALITY_UNSPECIFIED;
            }
            case AGGREGATION_TEMPORALITY_CUMULATIVE -> {
                return AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
            }
            case AGGREGATION_TEMPORALITY_DELTA -> {
                return AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
            }
        }
        log.warn("Unsupported AggregationTemporality: {}", aggregationTemporality);
        return null;
    }

    private List<HistogramDataPoint> mapHistogramDataPoints(List<io.opentelemetry.proto.metrics.v1.HistogramDataPoint> dataPointsList) {
        return dataPointsList.stream()
                .map(dp -> HistogramDataPoint.builder()
                        .attributes(this.commonModelMapper.mapKeyValueList(dp.getAttributesList()))
                        .max(dp.getMax())
                        .min(dp.getMin())
                        .sum(dp.getSum())
                        .count(dp.getCount())
                        .bucketCounts(dp.getBucketCountsList())
                        .explicitBounds(dp.getExplicitBoundsList())
                        .exemplars(mapExemplars(dp.getExemplarsList()))
                        .startTimeUnixNano(dp.getStartTimeUnixNano())
                        .timeUnixNano(dp.getTimeUnixNano())
                        .flags(dp.getFlags())
                        .build())
                .toList();
    }

    private List<NumberDataPoint> mapDataPoints(List<io.opentelemetry.proto.metrics.v1.NumberDataPoint> dataPointsList) {
        return dataPointsList.stream()
                .map(dp -> NumberDataPoint.builder()
                        .value(mapValue(dp))
                        .attributes(this.commonModelMapper.mapKeyValueList(dp.getAttributesList()))
                        .startTimeUnixNano(dp.getStartTimeUnixNano())
                        .timeUnixNano(dp.getTimeUnixNano())
                        .exemplars(mapExemplars(dp.getExemplarsList()))
                        .flags(dp.getFlags())
                        .build())
                .toList();
    }

    private List<ExponentialHistogramDataPoint> mapExponentialHistogramDataPoints(
            List<io.opentelemetry.proto.metrics.v1.ExponentialHistogramDataPoint> dataPointsList) {
        return dataPointsList.stream()
                .map(dp -> ExponentialHistogramDataPoint.builder()
                        .attributes(this.commonModelMapper.mapKeyValueList(dp.getAttributesList()))
                        .startTimeUnixNano(dp.getStartTimeUnixNano())
                        .timeUnixNano(dp.getTimeUnixNano())
                        .count(dp.getCount())
                        .sum(dp.hasSum() ? dp.getSum() : null)
                        .scale(dp.getScale())
                        .zeroCount(dp.getZeroCount())
                        .positive(mapBuckets(dp.getPositive()))
                        .negative(mapBuckets(dp.getNegative()))
                        .flags(dp.getFlags())
                        .exemplars(mapExemplars(dp.getExemplarsList()))
                        .min(dp.hasMin() ? dp.getMin() : null)
                        .max(dp.hasMax() ? dp.getMax() : null)
                        .zeroThreshold(dp.getZeroThreshold())
                        .build())
                .toList();
    }

    private ExponentialHistogramDataPoint.Buckets mapBuckets(
            io.opentelemetry.proto.metrics.v1.ExponentialHistogramDataPoint.Buckets buckets) {
        return ExponentialHistogramDataPoint.Buckets.builder()
                .offset(buckets.getOffset())
                .bucketCounts(buckets.getBucketCountsList())
                .build();
    }

    private List<SummaryDataPoint> mapSummaryDataPoints(List<io.opentelemetry.proto.metrics.v1.SummaryDataPoint> dataPointsList) {
        return dataPointsList.stream()
                .map(dp -> SummaryDataPoint.builder()
                        .attributes(this.commonModelMapper.mapKeyValueList(dp.getAttributesList()))
                        .startTimeUnixNano(dp.getStartTimeUnixNano())
                        .timeUnixNano(dp.getTimeUnixNano())
                        .count(dp.getCount())
                        .sum(dp.getSum())
                        .quantileValues(mapQuantileValues(dp.getQuantileValuesList()))
                        .flags(dp.getFlags())
                        .build())
                .toList();
    }

    private List<SummaryDataPoint.ValueAtQuantile> mapQuantileValues(
            List<io.opentelemetry.proto.metrics.v1.SummaryDataPoint.ValueAtQuantile> quantileValuesList) {
        return quantileValuesList.stream()
                .map(qv -> SummaryDataPoint.ValueAtQuantile.builder()
                        .quantile(qv.getQuantile())
                        .value(qv.getValue())
                        .build())
                .toList();
    }

    private List<Exemplar> mapExemplars(List<io.opentelemetry.proto.metrics.v1.Exemplar> exemplarsList) {
        return exemplarsList.stream()
                .map(e -> Exemplar.builder()
                        .filteredAttributes(this.commonModelMapper.mapKeyValueList(e.getFilteredAttributesList()))
                        .timeUnixNano(e.getTimeUnixNano())
                        .value(mapExemplarValue(e))
                        .spanId(bytesToHex(e.getSpanId().toByteArray()))
                        .traceId(bytesToHex(e.getTraceId().toByteArray()))
                        .build())
                .toList();
    }

    private Number mapExemplarValue(io.opentelemetry.proto.metrics.v1.Exemplar e) {
        switch (e.getValueCase()) {
            case AS_INT -> {
                return e.getAsInt();
            }
            case AS_DOUBLE -> {
                return e.getAsDouble();
            }
        }
        return null;
    }

    private Number mapValue(io.opentelemetry.proto.metrics.v1.NumberDataPoint dp) {
        switch(dp.getValueCase()) {
            case AS_INT -> {
                return dp.getAsInt();
            }
            case AS_DOUBLE -> {
                return dp.getAsDouble();
            }
        }
        return null;
    }
}
