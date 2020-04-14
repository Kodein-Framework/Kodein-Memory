buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://dl.bintray.com/kodein-framework/Kodein-Internal-Gradle")
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:2.13.0")
    }
}

apply { plugin("org.kodein.settings") }

rootProject.name = "Kodein-Memory"

include(
        "kodein-memory",
        "kodein-file",
        ""
)
