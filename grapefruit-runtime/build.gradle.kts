plugins {
    id("java")
    id("grapefruit.common-conventions")
}

dependencies {
    compileOnly(libs.annotations)
    implementation(libs.geantyref)
}
