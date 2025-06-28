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
    implementation(libs.clikt)
    implementation(libs.koog.agents)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

buildConfig {
    buildConfigField("String", "VERSION", "\"${version}\"")
}

application {
    mainClass.set("org.example.MainKt")
}
