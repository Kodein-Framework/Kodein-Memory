buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://dl.bintray.com/kodein-framework/Kodein-Internal-Gradle")
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:2.9.7")
    }
}

apply { plugin("org.kodein.settings") }

rootProject.name = "Kodein-Memory"

include(
        "kodein-memory",
        ""
)
