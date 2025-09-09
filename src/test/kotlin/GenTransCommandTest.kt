import ai.koog.agents.testing.tools.getMockExecutor
import ai.koog.agents.testing.tools.mockLLMAnswer
import com.github.ajalt.clikt.command.test
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.example.GenTransCommand

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
        val command = GenTransCommand({ _, _ -> mockLLMApi })
        val result = command.test(argv = "こんにちは世界")

        result.stdout shouldBe "Hello World!\n"
        result.statusCode shouldBe 0
    }

    "test GenTransCommand with stdin" {
        val command = GenTransCommand({ _, _ -> mockLLMApi })
        System.setIn("こんにちは世界\n".byteInputStream())
        val result = command.test(argv = "")

        result.stdout shouldBe "Hello World!\n"
        result.statusCode shouldBe 0
    }

    "test GenTransCommand with --to option" {
        val command = GenTransCommand({ _, _ -> mockLLMApi })
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

        val command = GenTransCommand({ _, _ -> summaryMockLLMApi })
        val result = command.test(argv = arrayOf("--summary", "これは長いテキストの内容です"))

        result.stdout shouldBe "Summarized: Hello\n"
        result.statusCode shouldBe 0
    }
})
