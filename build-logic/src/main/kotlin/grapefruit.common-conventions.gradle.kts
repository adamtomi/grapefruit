plugins {
    java
    jacoco
}

repositories {
    mavenCentral()
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

dependencies {
    testImplementation(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
    testImplementation(libs.jupiter.params)
}
