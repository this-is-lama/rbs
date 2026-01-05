plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}


dependencies {
    // Reactive Gateway (WebFlux + Netty)
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")

    // Service Discovery
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // Reactive Security (для фильтров в Gateway)
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.test { useJUnitPlatform() }
