plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.ktlint)
}

group = "io.github.hiroaki404"
version = libs.versions.cliVersion.get()

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.gentransCore)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.koog.agents.test)
    implementation(libs.clikt)
    implementation(libs.koog.agents)
    implementation(libs.slf4j.simple)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.koog.agents.features.opentelemetry)
}

tasks.test {
    useJUnitPlatform()
    val isCI = System.getenv("CI") == "true"
    val excludeTags = if (isCI) "integration" else ""
    systemProperty("kotest.tags.exclude", excludeTags)
}

// デバッグビルドかどうかの判定
val isRunTask = gradle.startParameter.taskNames.any { it.contains("run") }
val isDebugBuild = project.hasProperty("debug") &&
    project.property("debug").toString().toBoolean()
val enableDebug = isDebugBuild || isRunTask

buildConfig {
    packageName("io.github.hiroaki404.gentrans.cli")
    buildConfigField("String", "VERSION", "\"${version}\"")
    buildConfigField("Boolean", "IS_DEBUG", enableDebug.toString())
}

application {
    mainClass.set("io.github.hiroaki404.gentrans.cli.MainKt")
    applicationName = "gentrans"
}
