plugins {
    id("java")
}

group = "grapefruit"
version = "1.5.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:33.3.0-jre")
    implementation(project(":grapefruit-core"))
    annotationProcessor(project(":grapefruit-gen"))
}
