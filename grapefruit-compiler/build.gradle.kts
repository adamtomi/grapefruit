import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

apply(from = rootProject.file("buildSrc/shared.gradle.kts"))

dependencies {
    implementation(libs.auto.service)
    annotationProcessor(libs.auto.service)
    implementation(libs.geantyref)
    implementation(libs.javapoet)
    implementation(project(":grapefruit-runtime"))

    testImplementation(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
    testImplementation(libs.jupiter.params)
}

tasks.named<ShadowJar>("shadowJar") {
    minimize()
    archiveFileName.set("${project.parent?.group}-compiler-${project.parent?.version}.jar")
}
