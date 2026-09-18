plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.buildconfig)
}

group = "io.github.hiroaki404"
version = libs.versions.cliVersion.get()

repositories {
    mavenCentral()
}

configurations.all {
    exclude(group = "io.ktor")
}

dependencies {
    implementation(projects.gentransCore)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.koog.agents.test)
    implementation(libs.clikt)
    implementation(libs.koog.agents.core)
    runtimeOnly(libs.slf4j.simple)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.koog.agents.features.opentelemetry)
}

tasks.test {
    useJUnitPlatform()
    val isCI = providers.environmentVariable("CI").orElse("false").map { it == "true" }
    val excludeTags = isCI.map { ci -> if (ci) "smoke,integration" else "smoke" }
    systemProperty("kotest.tags.exclude", excludeTags.get())
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
