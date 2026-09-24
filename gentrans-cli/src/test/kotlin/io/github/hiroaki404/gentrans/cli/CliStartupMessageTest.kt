package io.github.hiroaki404.gentrans.cli

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Path

class CliStartupMessageTest : StringSpec({
    "CLI does not write kotlin-logging startup message to stdout" {
        val process = ProcessBuilder(
            Path.of(System.getProperty("java.home"), "bin", "java").toString(),
            "-cp",
            testRuntimeClasspath(),
            CliStartupMessageFixture::class.java.name,
        ).start()

        val stdout = process.inputStream.bufferedReader().readText()
        process.errorStream.bufferedReader().readText()

        process.waitFor() shouldBe 0
        stdout shouldBe ""
    }
})

object CliStartupMessageFixture {
    @JvmStatic
    fun main(args: Array<String>) {
        suppressKotlinLoggingStartupMessage()
        KotlinLogging.logger {}
    }
}

private fun testRuntimeClasspath(): String {
    val applicationClasspath = generateSequence(CliStartupMessageFixture::class.java.classLoader) { it.parent }
        .filterIsInstance<URLClassLoader>()
        .flatMap { it.getURLs().asSequence() }
        .map { Path.of(it.toURI()).toString() }
        .toList()

    return (System.getProperty("java.class.path").split(File.pathSeparator) + applicationClasspath)
        .distinct()
        .joinToString(File.pathSeparator)
}
