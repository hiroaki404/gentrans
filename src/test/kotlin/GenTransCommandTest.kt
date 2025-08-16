import ai.koog.agents.testing.tools.getMockExecutor
import ai.koog.agents.testing.tools.mockLLMAnswer
import com.github.ajalt.clikt.command.test
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.example.GenTransCommand

class GenTransCommandTest : StringSpec({
    val mockLLMApi = getMockExecutor {
        mockLLMAnswer("Bonjour le monde!") onCondition { it.contains("French") && it.contains("こんにちは世界") }
        mockLLMAnswer("Hello World!") onCondition { it.contains("English") && it.contains("こんにちは世界") }
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
