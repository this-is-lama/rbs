plugins {
    id("java-library")
}

val jwtVersion = "0.13.0"

dependencies {

    api("org.springframework:spring-context")
    api("org.springframework:spring-web")
    api("org.springframework:spring-webmvc")

    api("jakarta.servlet:jakarta.servlet-api")

    api("com.fasterxml.jackson.core:jackson-databind")

    api("org.springframework.security:spring-security-core")
    api("org.springframework.security:spring-security-oauth2-jose")
    api("org.springframework.security:spring-security-oauth2-resource-server")

    api("org.springframework.boot:spring-boot-starter-validation")

    //jwt
    implementation("io.jsonwebtoken:jjwt-api:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jwtVersion")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

}

tasks.test {
    useJUnitPlatform()
}