package io.github.hiroaki404.gentrans.core.api

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.testing.tools.getMockExecutor
import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.ModerationResult
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.ResponseMetaInfo
import ai.koog.prompt.streaming.StreamFrame
import ai.koog.utils.time.KoogClock
import io.github.hiroaki404.gentrans.core.markdown.PlaceholderMismatchException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList

/**
 * A [PromptExecutor] test double that "translates" by echoing back the source text embedded in
 * the translate prompt (between its `---\n\n` marker and the end), and answers every language
 * detection/decision prompt with a fixed language. Used to verify Markdown round-trips through
 * the whole [Translator] pipeline without depending on a real LLM's behavior.
 */
private class IdentityPromptExecutor(private val language: String = "English") : PromptExecutor() {
    override suspend fun execute(prompt: Prompt, model: LLModel, tools: List<ToolDescriptor>): Message.Assistant {
        val lastUserText = prompt.messages.filterIsInstance<Message.User>().last().textContent()
        val content = when {
            lastUserText.startsWith("Translate the following text from") -> lastUserText.substringAfter("---\n\n")
            else -> language
        }
        return Message.Assistant(content, ResponseMetaInfo(timestamp = KoogClock.System.now()))
    }

    override fun executeStreaming(prompt: Prompt, model: LLModel, tools: List<ToolDescriptor>): Flow<StreamFrame> =
        throw UnsupportedOperationException("not used by these tests")

    override suspend fun moderate(prompt: Prompt, model: LLModel): ModerationResult =
        throw UnsupportedOperationException("not used by these tests")

    override fun close() = Unit
}

private fun readGoldenMarkdown(path: String): String {
    val stream = object {}.javaClass.classLoader.getResourceAsStream(path)
        ?: error("resource not found: $path")
    return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
}

/** Collapses every run of 2+ newlines (with optional surrounding whitespace) into one blank line. */
private fun normalizeBlankLines(text: String): String =
    text.trim().replace(Regex("[ \\t]*\\n\\s*\\n[ \\t\\n]*"), "\n\n")

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

    context("MARKDOWN input format") {
        test("front matter and fenced code round-trip untouched while surrounding text is translated") {
            val markdown = "---\ntitle: Doc\n---\n\nこんにちは\n\n```kotlin\nval x = 1\n```\n"
            val mockLLM = getMockExecutor {
                mockLLMAnswer("Japanese") onCondition { it.contains("Identify the natural language of the following text") }
                mockLLMAnswer("English") onCondition { it.contains("Determine the target language for translation") }
                mockLLMAnswer("GENTRANS_BLOCK_0\n\nHello\n\nGENTRANS_BLOCK_1") onCondition {
                    it.contains("Translate the following text from Japanese to English")
                }
            }
            val translator = createTranslator(config, executorFactory = { mockLLM })

            val result = translator.translateToText(
                TranslationRequest(text = markdown, inputFormat = InputFormat.MARKDOWN)
            )

            result shouldBe "---\ntitle: Doc\n---\n\nHello\n\n```kotlin\nval x = 1\n```"
        }

        test("a chunk that is only a placeholder skips the LLM call and restores its block directly") {
            val markdown = "```kotlin\nval x = 1\n```\n"
            val mockLLM = getMockExecutor {
                mockLLMAnswer("Japanese") onCondition { it.contains("Identify the natural language of the following text") }
                mockLLMAnswer("English") onCondition { it.contains("Determine the target language for translation") }
                // Deliberately no answer for "Translate the following text..." - if the
                // implementation wrongly calls the LLM for this placeholder-only chunk, the mock
                // falls back to its (non-placeholder) default response and `restore` throws
                // PlaceholderMismatchException, failing this test.
            }
            val translator = createTranslator(config, executorFactory = { mockLLM })

            val result = translator.translateToText(
                TranslationRequest(text = markdown, inputFormat = InputFormat.MARKDOWN)
            )

            result shouldBe "```kotlin\nval x = 1\n```"
        }

        test("a translation that drops a placeholder fails the Flow with PlaceholderMismatchException") {
            val markdown = "Hello\n\n```kotlin\nval x = 1\n```\n"
            val mockLLM = getMockExecutor {
                mockLLMAnswer("Japanese") onCondition { it.contains("Identify the natural language of the following text") }
                mockLLMAnswer("English") onCondition { it.contains("Determine the target language for translation") }
                mockLLMAnswer("Hola") onCondition { it.contains("Translate the following text from Japanese to English") }
            }
            val translator = createTranslator(config, executorFactory = { mockLLM })

            val exception = shouldThrow<PlaceholderMismatchException> {
                translator.translate(TranslationRequest(text = markdown, inputFormat = InputFormat.MARKDOWN)).toList()
            }
            exception.missing shouldBe listOf(0)
        }

        test("multiple chunks are translated in order and joined with a blank line") {
            val paraOne = "PARA_ONE " + "x".repeat(6_000)
            val paraTwo = "PARA_TWO " + "y".repeat(6_000)
            val markdown = "$paraOne\n\n$paraTwo\n"
            val mockLLM = getMockExecutor {
                mockLLMAnswer("Japanese") onCondition { it.contains("Identify the natural language of the following text") }
                mockLLMAnswer("English") onCondition { it.contains("Determine the target language for translation") }
                mockLLMAnswer("TRANSLATED_ONE") onCondition {
                    it.contains("Translate the following text from Japanese to English") && it.contains("PARA_ONE")
                }
                mockLLMAnswer("TRANSLATED_TWO") onCondition {
                    it.contains("Translate the following text from Japanese to English") && it.contains("PARA_TWO")
                }
            }
            val translator = createTranslator(config, executorFactory = { mockLLM })

            val events = translator.translate(
                TranslationRequest(text = markdown, inputFormat = InputFormat.MARKDOWN)
            ).toList()

            events shouldBe listOf(
                TranslationEvent.SourceLanguageDetected(language = "Japanese", totalChunks = 2),
                TranslationEvent.TargetLanguageDecided(language = "English"),
                TranslationEvent.ChunkTranslated(
                    chunk = "TRANSLATED_ONE",
                    translatedTextSoFar = "TRANSLATED_ONE",
                    translatedChunks = 1,
                    totalChunks = 2,
                ),
                TranslationEvent.ChunkTranslated(
                    chunk = "TRANSLATED_TWO",
                    translatedTextSoFar = "TRANSLATED_ONE\n\nTRANSLATED_TWO",
                    translatedChunks = 2,
                    totalChunks = 2,
                ),
                TranslationEvent.Completed(text = "TRANSLATED_ONE\n\nTRANSLATED_TWO"),
            )
        }
    }

    context("MARKDOWN round-trip with an identity translator") {
        val fixtures = listOf(
            "heading-and-paragraphs",
            "lists",
            "table",
            "blockquote",
            "html-block",
            "horizontal-rule",
            "oversized-block",
        )

        fixtures.forEach { name ->
            test("'$name' round-trips modulo blank-line counts") {
                val markdown = readGoldenMarkdown("markdown/golden/$name.md")
                val translator = createTranslator(config, executorFactory = { IdentityPromptExecutor() })

                val result = translator.translateToText(
                    TranslationRequest(text = markdown, inputFormat = InputFormat.MARKDOWN)
                )

                normalizeBlankLines(result) shouldBe normalizeBlankLines(markdown)
            }
        }

        test("the repository README round-trips modulo blank-line counts") {
            val markdown = readGoldenMarkdown("markdown/roundtrip/README.md")
            val translator = createTranslator(config, executorFactory = { IdentityPromptExecutor() })

            val result = translator.translateToText(
                TranslationRequest(text = markdown, inputFormat = InputFormat.MARKDOWN)
            )

            normalizeBlankLines(result) shouldBe normalizeBlankLines(markdown)
        }
    }

    context("shouldSummary combined with MARKDOWN (observed behavior, not specially handled)") {
        test("a protected placeholder chunk flows into the summary prompt as ordinary text") {
            val markdown = "Intro.\n\n```kotlin\nval x = 1\n```\n\nOutro.\n"
            val mockLLM = getMockExecutor {
                mockLLMAnswer("Japanese") onCondition { it.contains("Identify the natural language of the following text") }
                mockLLMAnswer("English") onCondition { it.contains("Determine the target language for translation") }
                // The summary prompt sees the protected chunk verbatim, placeholder included, and
                // this mock happens to preserve it - so it survives into translateByLLM.
                mockLLMAnswer("Summary: GENTRANS_BLOCK_0") onCondition { it.contains("Summarize the following text") }
                mockLLMAnswer("Summary translated: GENTRANS_BLOCK_0") onCondition {
                    it.contains("Translate the following text from Japanese to English")
                }
            }
            val translator = createTranslator(config, executorFactory = { mockLLM })

            val result = translator.translateToText(
                TranslationRequest(text = markdown, shouldSummary = true, inputFormat = InputFormat.MARKDOWN)
            )

            // The fence content survives because the mock summary happened to keep the
            // placeholder token intact; a real LLM summarizing free-form text is not guaranteed
            // to do so, in which case `restore` would throw PlaceholderMismatchException instead.
            result shouldBe "Summary translated: ```kotlin\nval x = 1\n```"
        }
    }
})
