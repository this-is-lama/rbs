import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension


plugins {
    id("org.springframework.boot") version "4.0.8" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "my.project"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    the<DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.8")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.3")
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
