plugins {
    id("java-library")
}

dependencies {

    api("org.springframework:spring-web")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

}

tasks.test {
    useJUnitPlatform()
}