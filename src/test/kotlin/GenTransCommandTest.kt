import ai.koog.agents.testing.tools.getMockExecutor
import ai.koog.agents.testing.tools.mockLLMAnswer
import com.github.ajalt.clikt.command.test
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.example.GenTransCommand

class GenTransCommandTest : StringSpec({
    val mockLLMApi = getMockExecutor {
        // 言語検出プロンプト用のモック
        mockLLMAnswer("Japanese") onCondition { it.contains("Identify the natural language of the following text") }

        // 日本語→英語翻訳の場合（targetLanguageが未指定）
        mockLLMAnswer("English") onCondition {
            it.contains("Determine the target language for translation") &&
                it.contains("Input Text Language: Japanese") &&
                it.contains("Native Language:") &&
                it.contains("Second Language:")
        }

        // フランス語翻訳の場合（--toオプション指定）
        mockLLMAnswer("French") onCondition {
            it.contains("Convert this language identifier to standard English: French")
        }

        // 実際の翻訳プロンプト用のモック（英語）
        mockLLMAnswer("Hello World!") onCondition {
            it.contains("Translate the following text from Japanese to English")
        }

        // 実際の翻訳プロンプト用のモック（フランス語）
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
})
