plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Data
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    //MinIO
    implementation("io.minio:minio:8.6.0")

    // Security (если у тебя роль ADMIN/USER и т.п.)
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Cache
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Observability
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Service Discovery + Inter-service calls
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

    // testImplementation("org.testcontainers:redis")
}

tasks.test { useJUnitPlatform() }
