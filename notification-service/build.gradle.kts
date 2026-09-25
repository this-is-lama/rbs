plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}


val mapstructVersion = "1.6.3"
val lombokMapstruct = "0.2.0"


dependencies {

    implementation(project(":common-logging"))

    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // Трейсинг: Micrometer Tracing + OpenTelemetry, спаны уходят в Tempo по OTLP
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")

    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    // В Spring Boot 4 одного liquibase-core мало — без стартера миграции при старте не запускаются
    implementation("org.springframework.boot:spring-boot-starter-liquibase")

    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // В Spring Boot 4 автонастройка Kafka (ProducerFactory, ConsumerFactory, spring.kafka.*) живёт в отдельном стартере
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    implementation("org.springframework.boot:spring-boot-starter-mail")

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")


    //mapstruct
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstruct")
}

tasks.test {
    useJUnitPlatform()
}
