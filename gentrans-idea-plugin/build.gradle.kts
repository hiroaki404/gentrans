import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

// Not using the buildSrc convention plugin: it pins jvmToolchain(17), but IDEA 261 runs on JBR 21.
plugins {
    kotlin("jvm")
    // No version here: settings.gradle.kts already applies this plugin at 2.11.0 on the root
    // classpath; redeclaring a version here would conflict with that resolution.
    id("org.jetbrains.intellij.platform")
}

group = "io.github.hiroaki404"
version = libs.versions.gentrans.get()

// Locally installed IDEs, used instead of downloading a platform artifact.
// Override via -Pgentrans.localIdePath / -Pgentrans.localIdeaPath for a different machine/CI.
val localAndroidStudioPath: String =
    (findProperty("gentrans.localIdePath") as String?)
        ?: "${System.getProperty("user.home")}/Applications/Android Studio.app"
val localIdeaPath: String =
    (findProperty("gentrans.localIdeaPath") as String?)
        ?: "${System.getProperty("user.home")}/Applications/IntelliJ IDEA.app"

// "remote" is unused for now; kept as the documented path to a resolved platform artifact for
// the headless CI designed in Phase 4.
val platformSource: String = (findProperty("platformSource") as String? ?: "local").lowercase()

kotlin {
    jvmToolchain(21)
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_3)
        apiVersion.set(KotlinVersion.KOTLIN_2_3)
    }
}

dependencies {
    intellijPlatform {
        if (platformSource == "remote") {
            // useInstaller = false keeps this reproducible on a future CI with no local IDE.
            intellijIdea("2026.1") {
                useInstaller = false
            }
        } else {
            local(localAndroidStudioPath)
        }
        testFramework(TestFrameworkType.Platform)
    }

    testImplementation("junit:junit:4.13.2")
    // Only for scripting Translator's LLM responses in TranslationServiceTest; never shipped
    // (test classpath doesn't reach buildPlugin's output). Same platform-supplies-these
    // exclusions as gentransCore below - agents-test-jvm declares vanilla kotlinx-coroutines-core
    // directly, which otherwise shadows the platform's patched build on the test classpath.
    testImplementation(libs.koog.agents.test) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-jdk8")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "io.ktor")
    }

    implementation(projects.gentransCore) {
        // The platform already bundles kotlin-stdlib/kotlinx-coroutines-core on its classloader
        // (local-docs/10-design-final.md 2-3); shipping our own risks duplicate/conflicting classes.
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core-jvm")
        // Merged into kotlinx-coroutines-core in 1.7+ and already present on the platform classloader.
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-jdk8")
        // The platform supplies its own slf4j binding; shipping slf4j-api risks the wrong Logger impl.
        exclude(group = "org.slf4j", module = "slf4j-api")
        // gentrans-core already excludes io.ktor for itself; kept here in case that policy narrows.
        exclude(group = "io.ktor")
    }
    implementation(libs.jetbrains.markdown) {
        // Same platform-supplies-these policy as gentrans-core above; markdown-jvm:0.7.14
        // declares kotlin-stdlib:2.0.0, which isn't cached offline and must not be bundled anyway.
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
    }
}

intellijPlatform {
    // Booting a sandbox IDE to index the Settings UI instantiates every registered action too
    // (noisy Android Studio SEVERE logs, unrelated to us); revisit once that's no longer true.
    buildSearchableOptions = false

    pluginConfiguration {
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "261"
            // Left unset to stay compatible with future IDE builds instead of pinning one.
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        // EXPERIMENTAL_API_USAGES is deliberately left out: we allow @ApiStatus.Experimental
        // usage (JBHtmlPane, added in a later step).
        failureLevel = listOf(
            VerifyPluginTask.FailureLevel.INVALID_PLUGIN,
            VerifyPluginTask.FailureLevel.COMPATIBILITY_PROBLEMS,
            VerifyPluginTask.FailureLevel.MISSING_DEPENDENCIES,
            VerifyPluginTask.FailureLevel.INTERNAL_API_USAGES,
            VerifyPluginTask.FailureLevel.OVERRIDE_ONLY_API_USAGES,
        )
        // ktor SSE / logback / jackson MRJAR VarHandle / Kotlin synthetic annotation Impl -
        // known non-actionable noise from Koog/kotlinx transitive deps; see ignored-problems.txt.
        // That file has no comment support (the CLI loader splits each line on ':' before
        // compiling it as a regex), so keep it to bare patterns only.
        ignoredProblemsFile.set(file("verification/ignored-problems.txt"))
        ides {
            local(file(localAndroidStudioPath))
            local(file(localIdeaPath))
        }
    }
}

intellijPlatformTesting {
    runIde {
        // Default runIde targets Android Studio (see dependencies above); this adds a second
        // sandbox task against the local IntelliJ IDEA install.
        register("runIdea") {
            localPath = file(localIdeaPath)
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    from(rootProject.file("LICENSE")) {
        into("META-INF")
    }
    from(rootProject.file("THIRD_PARTY_NOTICES.md")) {
        into("META-INF")
    }
}
