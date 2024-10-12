plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {

    implementation(libs.guava)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Reference implementation for regression testing, assume of all places NGA can write
    // a correct TIFF file decoder lol
    testImplementation("mil.nga:tiff:3.0.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
