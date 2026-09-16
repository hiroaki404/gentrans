plugins {
    id("buildsrc.convention.kotlin-jvm")
}

group = "io.github.hiroaki404"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    implementation(libs.slf4j.simple)
    implementation(libs.koog.agents)
}

tasks.test {
    useJUnitPlatform()
}
