plugins {
    id("java-library")
}

dependencies {

    api("org.springframework.boot:spring-boot-starter-aspectj")
    api("org.slf4j:slf4j-api")

    compileOnly("org.springframework.security:spring-security-core")

    // для ActuatorObservationConfig; в рантайме эти классы есть у каждого сервиса
    compileOnly("io.micrometer:micrometer-observation")
    compileOnly("org.springframework:spring-web")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
}

tasks.test {
    useJUnitPlatform()
}
