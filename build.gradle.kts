plugins {
    alias(libs.plugins.ktlint)
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

subprojects {
    tasks.withType<Test> {
        jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED")
    }
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
