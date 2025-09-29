import org.gradle.api.JavaVersion.VERSION_21

plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "br.com.gabryel.waes.rdc"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
}

java {
    sourceCompatibility = VERSION_21
    targetCompatibility = VERSION_21
}