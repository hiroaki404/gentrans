package io.github.hiroaki404.gentrans.core.api

import ai.koog.agents.testing.tools.getMockExecutor
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList

@OptIn(InternalGentransApi::class)
class TranslatorTest : FunSpec({
    val config = TranslatorConfig(
        provider = Provider.OpenAI,
        model = "gpt-4o",
        apiKey = "dummy",
        nativeLanguage = "English",
        secondLanguage = "English",
    )

    val mockLLMApi = getMockExecutor {
        mockLLMAnswer("Japanese") onCondition { it.contains("Identify the natural language of the following text") }
        mockLLMAnswer("English") onCondition { it.contains("Determine the target language for translation") }
        mockLLMAnswer("Hello World!") onCondition { it.contains("Translate the following text from Japanese to English") }
    }

    test("translate emits events in order and translateToText returns the final result") {
        val translator = createTranslator(config, executorFactory = { mockLLMApi })

        val events = translator.translate(TranslationRequest(text = "こんにちは世界")).toList()

        events shouldBe listOf(
            TranslationEvent.SourceLanguageDetected(language = "Japanese", totalChunks = 1),
            TranslationEvent.TargetLanguageDecided(language = "English"),
            TranslationEvent.ChunkTranslated(
                chunk = "Hello World!",
                translatedTextSoFar = "Hello World!",
                translatedChunks = 1,
                totalChunks = 1,
            ),
            TranslationEvent.Completed(text = "Hello World!"),
        )

        val translatorForText = createTranslator(config, executorFactory = { mockLLMApi })
        val result = translatorForText.translateToText(TranslationRequest(text = "こんにちは世界"))
        result shouldBe "Hello World!"
    }
})
