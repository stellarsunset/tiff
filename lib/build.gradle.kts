import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-library`
    jacoco
    id("com.vanniktech.maven.publish") version "0.28.0"
}

repositories {
    mavenCentral()
}

dependencies {

    implementation(libs.guava)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Reference implementation for regression testing, assume of all places NGA can write
    // a correct TIFF file decoder
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

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required = true
        html.required = true
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestReport)
}

mavenPublishing {
    configure(JavaLibrary(javadocJar = JavadocJar.Javadoc(), sourcesJar = true))

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    coordinates("io.github.stellarsunset", "tiff", "0.0.3")

    pom {
        name = "tiff"
        description = "A data-driven library for interacting with common tiff image formats."
        url = "https://github.com/stellarsunset/tiff"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "stellarsunset"
                name = "Alex Cramer"
                email = "stellarsunset@proton.me"
            }
        }
        scm {
            connection = "scm:git:git://github.com/stellarsunset/tiff.git"
            developerConnection = "scm:git:ssh://github.com/stellarsunset/tiff.git"
            url = "http://github.com/stellarsunset/tiff"
        }
    }

    signAllPublications()
}