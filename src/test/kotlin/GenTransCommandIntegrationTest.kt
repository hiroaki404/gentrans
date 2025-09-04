import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import com.github.ajalt.clikt.command.test
import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.system.OverrideMode
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldContainIgnoringCase
import org.example.GenTransCommand

// This test is designed to be run after starting ollama on your local PC.
@Tags("integration")
class GenTransCommandIntegrationTest : StringSpec({
    val ollamaOptions = listOf("--provider", "ollama", "--model", "gemma3n:latest")

    fun createCommand() = GenTransCommand { _, _ ->
        SingleLLMPromptExecutor(OllamaClient())
    }

    "test GenTransCommand with real LLM" {
        val command = createCommand()
        val inputText = "こんにちは"
        val result = command.test(argv = ollamaOptions + inputText)

        result.stdout shouldContainIgnoringCase "hello"
        result.statusCode shouldBe 0
    }

    "test GenTransCommand with target language specified" {
        val command = createCommand()
        val inputText = "Hello"
        val result = command.test(argv = ollamaOptions + listOf("--to", "French", inputText))

        result.stdout shouldContainIgnoringCase "bonjour"
        result.statusCode shouldBe 0
    }

    "test GenTransCommand with English to Japanese translation" {
        val command = createCommand()
        val inputText = "Hello"
        val result = command.test(argv = ollamaOptions + listOf("--to", "Japanese", inputText))

        result.stdout shouldContain "こんにちは"
        result.statusCode shouldBe 0
    }

    "test GenTransCommand with short form target language option" {
        val command = createCommand()
        val inputText = "Thank you"
        val result = command.test(argv = ollamaOptions + listOf("-t", "Spanish", inputText))

        result.stdout shouldContainIgnoringCase "gracias"
        result.statusCode shouldBe 0
    }

    "test GenTransCommand with same language translation" {
        val command = createCommand()
        val inputText = "Hello"
        val result = command.test(argv = ollamaOptions + listOf("--to", "English", inputText))

        result.stdout shouldContainIgnoringCase "Hello"
        result.statusCode shouldBe 0
    }

    "test GenTransCommand with version option" {
        val command = createCommand()
        val result = command.test(argv = listOf("--version"))

        result.statusCode shouldBe 0
    }

    "test GenTransCommand with native language from environment variable" {
        withEnvironment("GENTRANS_NATIVE_LANGUAGE", "Japanese", OverrideMode.SetOrIgnore) {
            val command = createCommand()
            val inputText = "Hello"
            // Execute without specifying target language (auto language detection)
            val result = command.test(argv = ollamaOptions + inputText)

            result.stdout shouldContainIgnoringCase "こんにちは"
            result.statusCode shouldBe 0
        }
    }

    "test GenTransCommand with Japanese native language auto-translating to English" {
        withEnvironment("GENTRANS_NATIVE_LANGUAGE", "Japanese", OverrideMode.SetOrIgnore) {
            val command = createCommand()
            val inputText = "おはようございます"
            // Execute without specifying target language (auto language detection: Japanese → English)
            val result = command.test(argv = ollamaOptions + inputText)

            result.stdout shouldContainIgnoringCase "good morning"
            result.statusCode shouldBe 0
        }
    }

    "test GenTransCommand with second language from environment variable" {
        withEnvironment(
            mapOf(
                "GENTRANS_NATIVE_LANGUAGE" to "Japanese",
                "GENTRANS_SECOND_LANGUAGE" to "French"
            ),
            OverrideMode.SetOrIgnore
        ) {
            val command = createCommand()
            val inputText = "こんにちは"
            // Execute without specifying target language (auto language detection)
            val result = command.test(argv = ollamaOptions + inputText)

            result.stdout shouldContainIgnoringCase "bonjour"
            result.statusCode shouldBe 0
        }
    }
})
