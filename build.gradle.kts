plugins {
    id("java-library")
    `maven-publish`
    id("org.openjfx.javafxplugin") version "0.1.0"
}

base {
    group = "io.github.qupath"
    version = "0.2.0-rc1"
    description = "Extra classes built on JavaFX that are used to help create the QuPath user interface. " +
            "These don't depend on other QuPath modules, so can be reused elsewhere."
}


tasks.compileJava {
    options.encoding = "UTF-8"
    // Use the project's version or define one directly
    options.javaModuleVersion = provider { version as String }
}

sourceSets {
    create("controlsfx")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }

    withSourcesJar()
    withJavadocJar()

    // Optional ControlsFX support
    registerFeature("controlsfx") {
        usingSourceSet(sourceSets["controlsfx"])
    }
}

javafx {
    version = "17"
    modules = listOf(
                "javafx.base",
               "javafx.controls",
               "javafx.graphics"
                )
    configurations = arrayOf(
                "compileOnly",
                "implementation",
                "testImplementation"
                )
}

tasks.test {
    useJUnitPlatform()
}


/*
 * Manifest info
 */
tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Automatic-Module-Name" to "io.github.qupath.fxtras"))
    }
}

/*
 * Use -PstrictJavadoc=true to fail on error with doclint (which is rather strict).
 */
tasks.javadoc {
    val strictJavadoc = providers.gradleProperty("strictJavadoc").getOrElse("false")
    if ("true" != strictJavadoc) {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}


repositories {
    mavenCentral()
}

val controlsfxImplementation by configurations.existing
val compileOnly by configurations.existing {
    extendsFrom(controlsfxImplementation.get())
}
dependencies {
    api("org.slf4j:slf4j-api:2.0.0")

    // Optional ControlsFX support (used for notifications)
    controlsfxImplementation("org.controlsfx:controlsfx:11.1.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("ch.qos.logback:logback-classic:1.3.11")
}


publishing {
    repositories {
        maven {
            name = "SciJava"
            val releasesRepoUrl = uri("https://maven.scijava.org/content/repositories/releases")
            val snapshotsRepoUrl = uri("https://maven.scijava.org/content/repositories/snapshots")
            // Use gradle -Prelease publish
            url = if (project.hasProperty("release")) releasesRepoUrl else snapshotsRepoUrl
            credentials {
                username = System.getenv("MAVEN_USER")
                password = System.getenv("MAVEN_PASS")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                licenses {
                    license {
                        name = "Apache License v2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0"
                    }
                }
            }
        }
    }
}
