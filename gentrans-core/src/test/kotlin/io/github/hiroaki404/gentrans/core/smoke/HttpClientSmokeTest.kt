package io.github.hiroaki404.gentrans.core.smoke

import io.github.hiroaki404.gentrans.core.api.Provider
import io.github.hiroaki404.gentrans.core.api.TranslationRequest
import io.github.hiroaki404.gentrans.core.api.Translator
import io.github.hiroaki404.gentrans.core.api.TranslatorConfig
import io.kotest.assertions.fail
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.runBlocking

/**
 * Real-HTTP smoke tests. LOCAL/MANUAL only: needs network, excluded from the normal `test`
 * task and from CI. Run with `./gradlew :gentrans-core:smokeTest`.
 */
@Tags("smoke")
class HttpClientSmokeTest : FunSpec({

    test("ktor is absent from the runtime classpath") {
        try {
            Class.forName("io.ktor.client.HttpClient")
            fail("io.ktor.client.HttpClient should not be on the runtime classpath")
        } catch (_: ClassNotFoundException) {
            // expected: ktor was excluded from the build
        }
    }

    test("OpenAI request over JDK HTTP client fails with an auth error, proving the request was sent") {
        val translator = Translator(
            TranslatorConfig(
                provider = Provider.OpenAI,
                model = "gpt-4o-mini",
                apiKey = "invalid-key-for-smoke-test",
                nativeLanguage = "English",
                secondLanguage = "Japanese",
            ),
        )

        val thrown = runCatching {
            runBlocking { translator.translateToText(TranslationRequest("Hello")) }
        }.exceptionOrNull()

        requireNotNull(thrown) { "Expected an exception when calling OpenAI with an invalid API key" }
        // An auth error (rather than a connection/serialization failure) proves the JDK HTTP
        // client successfully reached OpenAI's server and got a real HTTP response back.
        assertMessageIndicatesAuthFailure(thrown)
    }

    test("Google request over JDK HTTP client fails with an auth error, proving the request was sent") {
        val translator = Translator(
            TranslatorConfig(
                provider = Provider.Google,
                model = "gemini-2.0-flash",
                apiKey = "invalid-key-for-smoke-test",
                nativeLanguage = "English",
                secondLanguage = "Japanese",
            ),
        )

        val thrown = runCatching {
            runBlocking { translator.translateToText(TranslationRequest("Hello")) }
        }.exceptionOrNull()

        requireNotNull(thrown) { "Expected an exception when calling Google with an invalid API key" }
        // An auth error (rather than a connection/serialization failure) proves the JDK HTTP
        // client successfully reached Google's server and got a real HTTP response back.
        assertMessageIndicatesAuthFailure(thrown)
    }
})

private val authFailureMarkers = listOf(
    "401",
    "403",
    "400",
    "api key",
    "unauthenticated",
    "permission_denied",
    "invalid",
)

private fun assertMessageIndicatesAuthFailure(thrown: Throwable) {
    val messages = generateSequence(thrown) { it.cause }.mapNotNull { it.message }.toList()
    val matched = messages.any { message -> authFailureMarkers.any { marker -> message.contains(marker, ignoreCase = true) } }
    if (!matched) {
        fail("Expected an auth-failure message (one of $authFailureMarkers) in the exception chain, got: $messages")
    }
}
