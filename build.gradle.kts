plugins {
    id("java")
    id("jacoco")
    // id("grapefruit.java-conventions")
}

group = "grapefruit"
version = "2.0.0-ALPHA"

repositories {
    mavenCentral()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

dependencies {
    testRuntimeOnly(libs.jupiter.engine)
}
