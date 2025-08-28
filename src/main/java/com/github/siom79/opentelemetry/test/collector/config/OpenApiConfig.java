package com.github.siom79.opentelemetry.test.collector.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "opentelemetry-test-collector API",
                version = "1.0.0",
                description = "REST API to list, clear and query metrics, traces and logs of the collector")
)
public class OpenApiConfig {

}
