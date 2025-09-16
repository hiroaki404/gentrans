// This is not used currentry, beacause koog is not yet target to macos, mingw, linux.
package buildsrc.convention

import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("multiplatform")
}

kotlin {
    // Use a specific Java version to make it easier to work in different environments.
    jvmToolchain(17)

    // Configure JVM target
    jvm()

    // Configure Native targets
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    // Configure common source sets
    sourceSets {
        commonMain {
            dependencies {
                // Add common dependencies here if needed
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        jvmMain {
            dependencies {
                // Add JVM-specific dependencies here if needed
            }
        }

        jvmTest {
            dependencies {
                // Add JVM test dependencies here if needed
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    // Configure all test Gradle tasks to use JUnitPlatform.
    useJUnitPlatform()

    // Log information about all test results, not only the failed ones.
    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED
        )
    }
}
