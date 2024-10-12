group = "grapefruit"
version = "2.0.0-ALPHA"

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
