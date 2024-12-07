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

    testImplementation(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
    testImplementation(libs.jupiter.params)
}

tasks.withType<Test>() {
    useJUnitPlatform()
}
