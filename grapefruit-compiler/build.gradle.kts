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
    implementation(libs.auto.service)
    annotationProcessor(libs.auto.service)
    implementation(libs.geantyref)
    implementation(libs.javapoet)
    implementation(project(":grapefruit-runtime"))
}
