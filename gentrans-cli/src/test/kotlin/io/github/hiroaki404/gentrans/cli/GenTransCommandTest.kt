package io.github.hiroaki404.gentrans.cli

import ai.koog.agents.testing.tools.getMockExecutor
import com.github.ajalt.clikt.command.test
import io.github.hiroaki404.gentrans.core.api.InternalGentransApi
import io.github.hiroaki404.gentrans.core.api.createTranslator
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

@OptIn(InternalGentransApi::class)
class GenTransCommandTest : StringSpec({
    val mockLLMApi = getMockExecutor {
        // Mock for language detection prompts
        mockLLMAnswer("Japanese") onCondition { it.contains("Identify the natural language of the following text") }

        // For Japanese → English translation (when targetLanguage is not specified)
        mockLLMAnswer("English") onCondition {
            it.contains("Determine the target language for translation") &&
                it.contains("Input Text Language: Japanese") &&
                it.contains("Native Language:") &&
                it.contains("Second Language:")
        }

        // For French translation (when --to option is specified)
        mockLLMAnswer("French") onCondition {
            it.contains("Convert this language identifier to standard English: French")
        }

        // Mock for actual translation prompts (English)
        mockLLMAnswer("Hello World!") onCondition {
            it.contains("Translate the following text from Japanese to English")
        }

        // Mock for actual translation prompts (French)
        mockLLMAnswer("Bonjour le monde!") onCondition {
            it.contains("Translate the following text from Japanese to French")
        }
    }

    "test GenTransCommand with argument" {
        val command = GenTransCommand { config -> createTranslator(config, executorFactory = { mockLLMApi }) }
        val result = command.test(argv = "こんにちは世界")

        result.stdout shouldBe "Hello World!\n"
        result.statusCode shouldBe 0
    }

    "test GenTransCommand with stdin" {
        val command = GenTransCommand { config -> createTranslator(config, executorFactory = { mockLLMApi }) }
        System.setIn("こんにちは世界\n".byteInputStream())
        val result = command.test(argv = "")

        result.stdout shouldBe "Hello World!\n"
        result.statusCode shouldBe 0
    }

    "test GenTransCommand with --to option" {
        val command = GenTransCommand { config -> createTranslator(config, executorFactory = { mockLLMApi }) }
        val result = command.test(argv = arrayOf("--to", "French", "こんにちは世界"))

        result.stdout shouldBe "Bonjour le monde!\n"
        result.statusCode shouldBe 0
    }

    "test GenTransCommand with --summary option" {
        val summaryMockLLMApi = getMockExecutor {
            // Mock for language detection prompts
            mockLLMAnswer("Japanese") onCondition { it.contains("Identify the natural language of the following text") }

            // For Japanese → English translation (when targetLanguage is not specified)
            mockLLMAnswer("English") onCondition {
                it.contains("Determine the target language for translation") &&
                    it.contains("Input Text Language: Japanese")
            }

            // Mock for summary processing
            mockLLMAnswer("Summarized: Hello") onCondition {
                it.contains("Summarize the following text") &&
                    it.contains("これは長いテキストの内容です")
            }

            // Mock for translation after summary
            mockLLMAnswer("Summarized: Hello") onCondition {
                it.contains("Translate the following text") &&
                    it.contains("Summarized: Hello")
            }
        }

        val command = GenTransCommand { config -> createTranslator(config, executorFactory = { summaryMockLLMApi }) }
        val result = command.test(argv = arrayOf("--summary", "これは長いテキストの内容です"))

        result.stdout shouldBe "Summarized: Hello\n"
        result.statusCode shouldBe 0
    }

    "test GenTransCommand with --format markdown option" {
        val markdownMockLLMApi = getMockExecutor {
            mockLLMAnswer("Japanese") onCondition { it.contains("Identify the natural language of the following text") }

            mockLLMAnswer("English") onCondition {
                it.contains("Determine the target language for translation") &&
                    it.contains("Input Text Language: Japanese")
            }

            mockLLMAnswer("# Hello\n\nThis is a test.\n\nGENTRANS_BLOCK_0") onCondition {
                it.contains("Translate the following text from Japanese to English")
            }
        }

        val markdownInput = "# こんにちは\n\nこれはテストです。\n\n```kotlin\nval x = 1\n```"

        val command = GenTransCommand { config -> createTranslator(config, executorFactory = { markdownMockLLMApi }) }
        val result = command.test(argv = arrayOf("--format", "markdown", markdownInput))

        result.stdout shouldBe "# Hello\n\nThis is a test.\n\n```kotlin\nval x = 1\n```\n"
        result.statusCode shouldBe 0
    }

    "test GenTransCommand with -m short option" {
        val markdownMockLLMApi = getMockExecutor {
            mockLLMAnswer("Japanese") onCondition { it.contains("Identify the natural language of the following text") }

            mockLLMAnswer("English") onCondition {
                it.contains("Determine the target language for translation") &&
                    it.contains("Input Text Language: Japanese")
            }

            mockLLMAnswer("# Hello\n\nThis is a test.\n\nGENTRANS_BLOCK_0") onCondition {
                it.contains("Translate the following text from Japanese to English")
            }
        }

        val markdownInput = "# こんにちは\n\nこれはテストです。\n\n```kotlin\nval x = 1\n```"

        val command = GenTransCommand { config -> createTranslator(config, executorFactory = { markdownMockLLMApi }) }
        val result = command.test(argv = arrayOf("-m", "markdown", markdownInput))

        result.stdout shouldBe "# Hello\n\nThis is a test.\n\n```kotlin\nval x = 1\n```\n"
        result.statusCode shouldBe 0
    }

    "test GenTransCommand with invalid --format value" {
        val command = GenTransCommand { config -> createTranslator(config, executorFactory = { mockLLMApi }) }
        val result = command.test(argv = arrayOf("--format", "foo", "こんにちは世界"))

        result.statusCode shouldBe 1
        result.output shouldContain "plain"
        result.output shouldContain "markdown"
    }

    "test GenTransCommand with refine summary for multiple chunks" {
        val refineMockLLMApi = getMockExecutor {
            // Mock for language detection prompts
            mockLLMAnswer("Japanese") onCondition { it.contains("Identify the natural language of the following text") }

            // For Japanese → English translation
            mockLLMAnswer("English") onCondition {
                it.contains("Determine the target language for translation") &&
                    it.contains("Input Text Language: Japanese")
            }

            // Mock for any summary prompts (both normal and refine)
            mockLLMAnswer("Summary: First chunk") onCondition {
                it.contains("Summarize the following text") &&
                    !it.contains("Existing Summary:")
            }

            // Mock for refine prompts
            mockLLMAnswer("Refined Summary: Combined chunks") onCondition {
                it.contains("Existing Summary:")
            }

            // Mock for final translation
            mockLLMAnswer("Translated: Combined summary") onCondition {
                it.contains("Translate the following text")
            }
        }

        // Create text that will definitely be split into multiple chunks (over 2000 chars per chunk)
        val chunk1 = "これは技術に関する最初のチャンクです。" + "A".repeat(2000)
        val chunk2 = "これはビジネスに関する2番目のチャンクです。" + "B".repeat(2000)
        val longText = "$chunk1\n$chunk2"

        val command = GenTransCommand { config -> createTranslator(config, executorFactory = { refineMockLLMApi }) }
        val result = command.test(argv = arrayOf("--summary", longText))

        result.stdout shouldBe "Translated: Combined summary\n"
        result.statusCode shouldBe 0
    }
})
