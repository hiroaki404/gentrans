import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

dependencyResolutionManagement {
    // Use Maven Central as the default repository (where Gradle will download dependencies) in all subprojects.
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        // Registers the repositories the IntelliJ Platform Gradle Plugin needs to resolve
        // intellijIdea()/androidStudio() platform artifacts and the Plugin Verifier IDEs
        // (gentrans-idea-plugin). Requires the settings-side "org.jetbrains.intellij.platform.settings"
        // plugin applied below, since dependencyResolutionManagement runs before project plugins.
        intellijPlatform {
            defaultRepositories()
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    // Version is hardcoded here (matches gradle/libs.versions.toml's intellijPlatformGradlePlugin)
    // because settings.gradle.kts plugins {} resolves before the project-level version catalog
    // is available.
    id("org.jetbrains.intellij.platform.settings") version "2.11.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "gentrans"
include(":gentrans-core")
include(":gentrans-cli")
include(":gentrans-idea-plugin")
