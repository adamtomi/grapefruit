plugins {
    id("java")
    id("jacoco")
    id("com.vanniktech.maven.publish") version "0.36.0"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.jspecify)
    implementation(libs.geantyref)

    testImplementation(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
    testImplementation(libs.jupiter.params)
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}
