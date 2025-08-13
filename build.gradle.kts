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
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jacoco {
    toolVersion = "0.8.12"
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

    // JSON Processing
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Utilities
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("com.opencsv:opencsv:5.9")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Testcontainers
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.withType<Test> {
    useJUnitPlatform()
    ignoreFailures = true // Tests bei Fehlern trotzdem weiterlaufen lassen

    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2).takeIf { it > 0 } ?: 1

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }

    finalizedBy(tasks.jacocoTestReport) // Immer danach Coverage generieren
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    mustRunAfter(tasks.test) // Auch bei Testfehlern

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")).include("**/*.exec"))
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)

    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.register("testWithCoverage") {
    group = "verification"
    description = "Runs tests and generates JaCoCo coverage report even if tests fail"
    dependsOn(tasks.test)
    finalizedBy(tasks.jacocoTestReport)

    doLast {
        println("Tests wurden ausgeführt (auch mit Fehlern) – Coverage-Report ist erstellt.")
    }
}