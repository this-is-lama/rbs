FROM gradle:8.8-jdk17 AS builder
WORKDIR /app

COPY . .
ARG MODULE
RUN gradle :${MODULE}:bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

ARG MODULE
COPY --from=builder /app/${MODULE}/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]