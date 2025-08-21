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
}

buildConfig {
    buildConfigField("String", "VERSION", "\"${version}\"")
}

application {
    mainClass.set("org.example.MainKt")
}
