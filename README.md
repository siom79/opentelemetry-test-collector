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

### OpenAPI

The following OpenAPI document shows the currently implemented API. As the implementation
is far from being finished, some operations still are missing.

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
          description: The list has been metrics
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
            "startTimeUnixMano": 1756412455705140500,
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