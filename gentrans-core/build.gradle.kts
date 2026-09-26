plugins {
    id("buildsrc.convention.kotlin-jvm")
}

group = "io.github.hiroaki404"

repositories {
    mavenCentral()
}

configurations.all {
    exclude(group = "io.ktor")
}

dependencies {
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.koog.agents.test)
    implementation(libs.slf4j.api)
    implementation(libs.koog.agents.core)
    implementation(libs.koog.client.openai)
    implementation(libs.koog.client.anthropic)
    implementation(libs.koog.client.ollama)
    implementation(libs.koog.client.google)
    implementation(libs.koog.http.client.java)
    implementation(libs.jetbrains.markdown)
}

tasks.test {
    useJUnitPlatform()
    systemProperty("kotest.tags.exclude", "smoke")
}

// LOCAL/MANUAL only: real-HTTP smoke tests that verify the ktor-free build can still reach
// the LLM providers. Needs network; not run as part of `test` or CI.
val smokeTest by tasks.registering(Test::class) {
    group = "verification"
    description = "Runs real-HTTP smoke tests (network required, not part of the normal test task)."
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform()
    systemProperty("kotest.tags.include", "smoke")
    outputs.upToDateWhen { false }
}

kotlin {
    explicitApi()
    compilerOptions {
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().matching { it.name.contains("Test") }.configureEach {
    compilerOptions.freeCompilerArgs.add("-Xexplicit-api=disable")
}
