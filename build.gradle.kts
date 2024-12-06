plugins {
    id("java")
}

group = "grapefruit"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.annotations)
    implementation(libs.geantyref)
}