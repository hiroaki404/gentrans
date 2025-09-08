plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.ktlint)
}

group = "org.example"
version = libs.versions.appVersion.get()

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

tasks.withType<Test> {
    jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED")
}

ktlint {
    filter {
        exclude("**/build/**")
        exclude { element -> element.file.path.contains("build/generated") }
    }
}

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

// ソース生成タスク
tasks.register("generateBuildInfo") {
    val outputDir = layout.buildDirectory.dir("generated/kotlin")
    outputs.dir(outputDir)

    doLast {
        val buildInfoFile = outputDir.get().file("org/example/BuildInfo.kt").asFile
        buildInfoFile.parentFile.mkdirs()
        buildInfoFile.writeText(
            """
            package org.example
            
            object BuildInfo {
                const val IS_DEBUG = $enableDebug
                const val VERSION = "$version"
            }
            """.trimIndent()
        )
    }
}

// メインのcompileKotlinタスクが生成されたソースを使用するように設定
tasks.named("compileKotlin") {
    dependsOn("generateBuildInfo")
}

// ktlintタスクもgenerateBuildInfoに依存するように設定
tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask> {
    dependsOn("generateBuildInfo")
}

sourceSets {
    main {
        kotlin {
            srcDir(layout.buildDirectory.dir("generated/kotlin"))
        }
    }
}

buildConfig {
    buildConfigField("String", "VERSION", "\"${version}\"")
}

application {
    mainClass.set("org.example.MainKt")
}
