# ---------- BUILD ----------
FROM gradle:8.14.5-jdk21 AS builder

WORKDIR /app

COPY . .

ARG MODULE

RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle :${MODULE}:bootJar -x test --no-daemon

# ---------- RUN ----------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

ARG MODULE

COPY --from=builder /app/${MODULE}/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]