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