# opentelemetry-test-collector

## Introduction

This project provides an OpenTelemetry collector that gathers metrics, 
traces, and logs sent by an OpenTelemetry exporter and makes them 
accessible through a REST API.

In this way, the OpenTelemetry test collector can be used in integration 
tests to verify that the export of metrics, traces, and logs works as 
expected by querying the provided REST API.

Since it also logs all incoming requests, it can additionally be used to 
inspect the OpenTelemetry requests sent by your application.

## Build & Run

After cloning the Git repository, you can build the server using the following command:

```bash
./gradlew build
```

Afterward you can run the server with:

```bash
./gradlew bootRun
```

## Usage

### Docker Container

A docker container is available through:

```bash
docker pull ghcr.io/siom79/opentelemetry-test-collector:main
```

The following environment variables can be set:

| Environment variable | Default value | Description                      |
|----------------------|---------------|----------------------------------|
| HTTP_SERVER_PORT     | 4318          | Port of the HTTP/Protobuf server |
| GRPC_SERVER_PORT     | 4317          | Port of the GRPC server          |
| TRACES_CACHE_SIZE    | 1000          | Size of the traces cache         |
| METRICS_CACHE_SIZE   | 1000          | Size of the metrics cache        |

### OpenAPI

The following OpenAPI document shows the currently implemented API.

```yaml
openapi: 3.0.1
info:
  title: opentelemetry-test-collector API
  description: "REST API to list, clear and query metrics, traces and logs of the\
    \ collector"
  version: 1.0.0
servers:
  - url: http://localhost:4318
    description: Generated server url
paths:
  /api/traces/clear:
    post:
      tags:
        - api-traces-controller
      summary: Clears the list of all captured traces
      operationId: clear
      responses:
        "200":
          description: The list has been cleared
  /api/metrics/clear:
    post:
      tags:
        - api-metrics-controller
      summary: Clears the list of all captured metrics
      operationId: clear_1
      responses:
        "200":
          description: The metrics list has been cleared
  /api/traces/list:
    get:
      tags:
        - api-traces-controller
      summary: Returns a list of all captured traces
      operationId: list
      responses:
        "200":
          description: Returns the list of traces
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: "#/components/schemas/ResourceSpans"
  /api/metrics/list:
    get:
      tags:
        - api-metrics-controller
      summary: Returns a list of all captured metrics
      operationId: list_1
      responses:
        "200":
          description: Returns the list of metrics
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: "#/components/schemas/ResourceMetrics"
```

The OpenAPI document is accessible through the URL `http://localhost:4318/api-docs.yaml`.

An interactive Swagger UI is available at `http://localhost:4318/swagger-ui.html`.

### Health Check

The application exposes a health endpoint at `http://localhost:4318/actuator/health`, which can be used as a liveness/readiness probe in Docker or Kubernetes deployments.

### Examples

The following request returns all captured traces:

```bash
curl --request GET \
  --url http://localhost:4318/api/traces/list
[
  {
    "schemaUrl": "",
    "resource": {
      ...
    },
    "scopeSpans": [
      {
        "spans": [
          {
            "traceId": "405c9c1ce6c77c8830e100f631e0e3bc",
            "spanId": "60340b8847147a07",
            "traceState": "",
            "parentSpanId": "",
            "flags": 257,
            "name": "http get /test",
            "spanKind": "SPAN_KIND_SERVER",
            "startTimeUnixNano": 1756412455705140500,
            "endTimeUnixNano": 1756412455706545000,
            "attributes": [
              {
                "key": "http.url",
                "value": {
                  "type": "STRING",
                  "stringValue": "/test"
                }
              },
              {
                "key": "method",
                "value": {
                  "type": "STRING",
                  "stringValue": "GET"
                }
              },
            ...
```

The following one clears the traces cache (e.g. before another test case):

```bash
curl --request POST \
  --url http://localhost:4318/api/traces/clear
```

### Testcontainers Integration

The test collector can be used in integration tests via [Testcontainers](https://testcontainers.com/) to verify that your Spring Boot application correctly exports metrics and traces.

#### Example: Verifying Traces

The following example starts the test collector as a Testcontainer, configures the OpenTelemetry SDK to export to it, and asserts that a specific span was captured:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TracesExportIT {

    @Container
    static GenericContainer<?> collector = new GenericContainer<>(
            "ghcr.io/siom79/opentelemetry-test-collector:main")
        .withExposedPorts(4317, 4318)
        .waitingFor(Wait.forHttp("/actuator/health")
            .forPort(4318)
            .forStatusCode(200));

    @DynamicPropertySource
    static void otelProperties(DynamicPropertyRegistry registry) {
        String endpoint = "http://localhost:" + collector.getMappedPort(4318);
        registry.add("otel.exporter.otlp.endpoint", () -> endpoint);
        registry.add("otel.traces.exporter", () -> "otlp");
    }

    @Test
    void myEndpoint_shouldExportSpan() throws Exception {
        // given
        String collectorBaseUrl = "http://localhost:" + collector.getMappedPort(4318);
        RestClient restClient = RestClient.create(collectorBaseUrl);
        restClient.post().uri("/api/traces/clear").retrieve().toBodilessEntity();

        // when — call the endpoint under test
        // restTemplate.getForEntity("/my-endpoint", String.class);

        // then — verify the span was exported
        await().atMost(10, SECONDS).untilAsserted(() -> {
            List<Map<String, Object>> traces = restClient.get()
                .uri("/api/traces/list")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

            assertThat(traces).isNotEmpty();
            assertThat(traces.getFirst())
                .extracting("scopeSpans")
                .asList()
                .isNotEmpty();
        });
    }
}
```

#### Example: Verifying Metrics

```java
@Test
void myService_shouldExportGaugeMetric() {
    String collectorBaseUrl = "http://localhost:" + collector.getMappedPort(4318);
    RestClient restClient = RestClient.create(collectorBaseUrl);
    restClient.post().uri("/api/metrics/clear").retrieve().toBodilessEntity();

    // trigger metric recording in the application under test

    await().atMost(15, SECONDS).untilAsserted(() -> {
        List<Map<String, Object>> metrics = restClient.get()
            .uri("/api/metrics/list")
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});

        assertThat(metrics).isNotEmpty();

        List<?> scopeMetrics = (List<?>) ((Map<?, ?>) metrics.getFirst()).get("scopeMetrics");
        List<?> metricList = (List<?>) ((Map<?, ?>) scopeMetrics.getFirst()).get("metrics");

        assertThat(metricList).anySatisfy(m -> {
            Map<?, ?> metric = (Map<?, ?>) m;
            assertThat(metric.get("name")).isEqualTo("my.gauge.metric");
        });
    });
}
```