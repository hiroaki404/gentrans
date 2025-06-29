plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    alias(libs.plugins.shadow)
    alias(libs.plugins.buildconfig)
}

group = "org.example"
version = libs.versions.appVersion.get()

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.koog.agents.test)
    implementation(libs.clikt)
    implementation(libs.koog.agents)
    implementation(libs.slf4j.simple)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

buildConfig {
    buildConfigField("String", "VERSION", "\"${version}\"")
}

application {
    mainClass.set("org.example.MainKt")
}
