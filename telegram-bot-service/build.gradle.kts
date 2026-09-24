plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}


dependencies {

    implementation(project(":common-logging"))

    // web нужен ради actuator (health/prometheus); своих REST-эндпоинтов у сервиса нет
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // Трейсинг: Micrometer Tracing + OpenTelemetry, спаны уходят в Tempo по OTLP
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")

    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    // В Spring Boot 4 автоконфигурация Liquibase вынесена в отдельный модуль:
    // одного liquibase-core мало — миграции при старте не запустятся
    implementation("org.springframework.boot:spring-boot-starter-liquibase")

    // черновики обращений (диалог с пользователем) живут в памяти с TTL
    implementation("com.github.ben-manes.caffeine:caffeine")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.test {
    useJUnitPlatform()
}
