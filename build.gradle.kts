plugins {
	java
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "de.finance"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot Starters (bereits von spring.io generiert)
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Banking Data Processing - NEUE DEPENDENCIES
	implementation("org.apache.commons:commons-csv:1.10.0")           // CSV Processing
	implementation("org.apache.poi:poi:5.2.4")                       // Excel/PDF Processing
	implementation("org.apache.poi:poi-ooxml:5.4.0")                 // Modern Excel formats
	implementation("org.apache.poi:poi-scratchpad:5.2.4")            // Legacy formats

	// JSON Processing für flexible Bankdaten
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // Kotlin JSON
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310") // Java Time API

	// Utilities
	implementation("org.apache.commons:commons-lang3:3.18.0")        // String/Collection Utils
	implementation("com.opencsv:opencsv:5.9")                        // Alternative CSV Library

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.testcontainers:junit-jupiter")           // Database Testing
	testImplementation("org.testcontainers:postgresql")              // PostgreSQL Testing

	// Eine Zeile für Swagger:
	// → http://localhost:8080/swagger-ui.html
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
}

// Gradle Performance Optimizations (für Java 24)
tasks.withType<JavaCompile> {
	options.compilerArgs.add("-parameters")
}

// Test Configuration
tasks.withType<Test> {
	useJUnitPlatform()
	// Parallel test execution
	maxParallelForks = Runtime.getRuntime().availableProcessors().div(2).takeIf { it > 0 } ?: 1
}