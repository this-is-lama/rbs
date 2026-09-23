plugins {
    id("java-library")
}

dependencies {

    api("org.springframework.boot:spring-boot-starter-aspectj")
    api("org.slf4j:slf4j-api")

    compileOnly("org.springframework.security:spring-security-core")
}

tasks.test {
    useJUnitPlatform()
}
