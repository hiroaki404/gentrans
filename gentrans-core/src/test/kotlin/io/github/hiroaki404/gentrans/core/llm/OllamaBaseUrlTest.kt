package io.github.hiroaki404.gentrans.core.llm

import com.sun.net.httpserver.HttpServer
import io.github.hiroaki404.gentrans.core.api.Provider
import io.github.hiroaki404.gentrans.core.api.TranslationRequest
import io.github.hiroaki404.gentrans.core.api.Translator
import io.github.hiroaki404.gentrans.core.api.TranslatorConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Proves [TranslatorConfig.ollamaBaseUrl] actually reaches the client, using a local loopback
 * server instead of a real Ollama install so this runs in CI without one.
 */
class OllamaBaseUrlTest : FunSpec({
    test("a translation request is sent to the configured Ollama base url, not the default") {
        val requestReceived = AtomicBoolean(false)
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            requestReceived.set(true)
            exchange.sendResponseHeaders(500, -1)
            exchange.close()
        }
        server.start()

        try {
            val translator = Translator(
                TranslatorConfig(
                    provider = Provider.Ollama,
                    model = "llama3.2",
                    apiKey = null,
                    nativeLanguage = "English",
                    secondLanguage = "Japanese",
                    ollamaBaseUrl = "http://127.0.0.1:${server.address.port}",
                ),
            )

            runCatching {
                runBlocking { translator.translateToText(TranslationRequest("Hello")) }
            }

            requestReceived.get() shouldBe true
        } finally {
            server.stop(0)
        }
    }
})
