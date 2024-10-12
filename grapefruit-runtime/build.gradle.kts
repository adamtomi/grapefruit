plugins {
    id("java")
    id("maven-publish")
    id("jacoco")
}

dependencies {
    compileOnly(libs.annotations)
    implementation(libs.geantyref)
}



/*


test {
    useJUnitPlatform()
}

test.finalizedBy jacocoTestReport
jacocoTestReport.dependsOn test

task sourcesJar(type: Jar) {
    archiveClassifier = 'sources'
    from sourceSets.main.java.srcDirs
}

artifacts {
    archives sourcesJar
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            artifactId = 'grapefruit'
            from components.java
            artifact sourcesJar

            versionMapping {
                usage('java-api') {
                    fromResolutionOf('runtimeClasspath')
                }
                usage('java-runtime') {
                    fromResolutionResult()
                }
            }
            pom {
                name = 'Grapefruit'
                description = 'A command parsing library'
                url = 'https://github.com/HgeX/grapefruit'
                licenses {
                    license {
                        name = 'GNU General Public License v3.0'
                        url = 'https://www.gnu.org/licenses/gpl-3.0.txt'
                    }
                }
            }

            pom.withXml {
                asNode().dependencies.'*'.findAll() {
                    it.scope.text() == 'runtime'  && project.configurations.default.allDependencies.find { dep ->
                        dep.name == it.artifactId.text()
                    }
                }.each() {
                    it.scope*.value = 'compile'
                }
            }
        }
    }

    repositories {
        maven {
            def releasesRepoUrl = layout.buildDirectory.dir('repos/releases')
            def snapshotsRepoUrl = layout.buildDirectory.dir('repos/snapshots')
            url = version.endsWith('SNAPSHOT') ? snapshotsRepoUrl : releasesRepoUrl
        }
    }
}
*/
