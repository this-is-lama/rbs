plugins {
    id("java")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

val mapstructVersion = "1.6.3"
val minioVersion = "8.6.0"
val lombokMapstruct = "0.2.0"

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")

    //MinIO
    implementation("io.minio:minio:$minioVersion")

    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")

    implementation ("org.springframework.boot:spring-boot-starter-validation")


    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    //mapstruct
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstruct")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

    // testImplementation("org.testcontainers:redis")
}

tasks.test { useJUnitPlatform() }
