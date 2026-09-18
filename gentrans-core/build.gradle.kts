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
    implementation(libs.slf4j.simple)
    implementation(libs.koog.agents.core)
    implementation(libs.koog.client.openai)
    implementation(libs.koog.client.anthropic)
    implementation(libs.koog.client.ollama)
    implementation(libs.koog.client.google)
    implementation(libs.koog.http.client.java)
}

tasks.test {
    useJUnitPlatform()
}
