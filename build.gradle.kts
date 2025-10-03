import org.gradle.api.JavaVersion.VERSION_21
import org.gradle.kotlin.dsl.sourceSets

plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "br.com.gabryel.waes.rdc"

repositories {
    mavenCentral()
}

sourceSets {
    val integrationTest by creating {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }

    configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
    configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql:42.7.3")

    implementation("me.paulschwarz:spring-dotenv:4.0.0")

    implementation("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    // Test
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:5.20.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.20.0")
    testImplementation("org.hamcrest:hamcrest:3.0")
    testImplementation("org.hobsoft.hamcrest:hamcrest-compose:0.5.0")

    // Integration Test
    "integrationTestImplementation"("org.springframework.boot:spring-boot-starter-test")
    "integrationTestImplementation"("org.springframework.boot:spring-boot-testcontainers")
    "integrationTestImplementation"("org.testcontainers:junit-jupiter")
    "integrationTestImplementation"("org.testcontainers:postgresql")
    "integrationTestRuntimeOnly"("com.h2database:h2:2.4.240")
}

java {
    sourceCompatibility = VERSION_21
    targetCompatibility = VERSION_21
}

tasks {
    test {
        useJUnitPlatform()
    }

    val integrationTest by registering(Test::class) {
        outputs.upToDateWhen { false } // Always reruns

        description = "Runs integration tests."
        group = "verification"

        useJUnitPlatform()

        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
    }

    named("check") { dependsOn(integrationTest, jacocoTestReport, jacocoTestCoverageVerification) }

    jacocoTestReport {
        dependsOn(test)

        configureExclusions()
    }

    jacocoTestCoverageVerification {
        dependsOn(test)

        configureExclusions()

        violationRules {
            rule {
                limit {
                    // Goal: 80%
                    // Most of the code for now is tested on ITs(Controllers/Repositories).
                    // In the future, should arrive in 80% just with the more unit testable parts
                    minimum = BigDecimal("0.73")
                }
            }
        }
    }
}

fun JacocoReportBase.configureExclusions() {
    classDirectories = files(classDirectories.files.map {
        fileTree(it) { exclude("**/dto/**", "**/entity/**") }
    })
}
