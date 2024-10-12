plugins {
    id("java")
    id("grapefruit.java-conventions")
}

dependencies {
    compileOnly(libs.annotations)
    implementation(libs.geantyref)

    testImplementation(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
    testImplementation(libs.jupiter.params)
}
