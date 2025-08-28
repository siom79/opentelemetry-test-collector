FROM amazoncorretto:21.0.4-alpine3.18

WORKDIR /app
COPY build/libs/opentelemetry-test-collector-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 4317
EXPOSE 4318

ENTRYPOINT ["java", "-jar", "app.jar"]