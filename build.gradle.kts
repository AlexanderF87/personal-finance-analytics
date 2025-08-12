plugins {
	java
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "de.finance"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

jacoco {
    toolVersion = "0.8.10"
}

repositories {
	mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")

    // Banking Data Processing
    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("org.apache.poi:poi:5.4.0")
    implementation("org.apache.poi:poi-ooxml:5.4.0")
    implementation("org.apache.poi:poi-scratchpad:5.4.0")

    // JSON Processing (jackson-module-kotlin only for Kotlin)
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Utilities
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("com.opencsv:opencsv:5.9")                        // Alternative zu commons-csv

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // Swagger → http://localhost:8080/swagger-ui.html
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    // Testing - spring-boot-starter-test enthält bereits ALLES was du brauchst
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine") // optional
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Zusätzliche Test-Dependencies
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
}

// Gradle Performance Optimizations (für Java 24)
tasks.withType<JavaCompile> {
	options.compilerArgs.add("-parameters")
}

// Test Configuration
tasks.withType<Test> {
    useJUnitPlatform()

    // Parallel test execution (bereits vorhanden)
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2).takeIf { it > 0 } ?: 1

    // Test-Logging hinzufügen
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    // Test-Reports
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}