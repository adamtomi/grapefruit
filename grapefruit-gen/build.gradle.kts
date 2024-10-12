plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "grapefruit"
version = "2.0.0-ALPHA"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.auto.service:auto-service:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    implementation("com.squareup:javapoet:1.13.0")
    implementation("io.leangen.geantyref:geantyref:2.0.0")
    implementation(project(":grapefruit-runtime"))
}
