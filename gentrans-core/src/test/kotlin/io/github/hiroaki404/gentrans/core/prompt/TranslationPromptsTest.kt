package io.github.hiroaki404.gentrans.core.prompt

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.message.Message
import io.github.hiroaki404.gentrans.core.api.InputFormat
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class TranslationPromptsTest : FunSpec({

    fun systemText(inputFormat: InputFormat): String {
        val built = prompt("test") {
            translatePrompt("Japanese", "English", "text", inputFormat)
        }
        return built.messages.filterIsInstance<Message.System>().first().textContent()
    }

    context("translatePrompt") {
        test("should include Markdown-preservation rules in the system prompt for MARKDOWN input") {
            val system = systemText(InputFormat.MARKDOWN)
            system shouldContain "Markdown"
            system shouldContain "GENTRANS_BLOCK_"
        }

        test("should not include Markdown-preservation rules in the system prompt for PLAIN_TEXT input") {
            val system = systemText(InputFormat.PLAIN_TEXT)
            system shouldNotContain "Markdown"
            system shouldNotContain "GENTRANS_BLOCK_"
        }

        test("should not change the PLAIN_TEXT system prompt when inputFormat is omitted") {
            val explicit = systemText(InputFormat.PLAIN_TEXT)
            val builtDefault = prompt("test") {
                translatePrompt("Japanese", "English", "text")
            }
            val default = builtDefault.messages.filterIsInstance<Message.System>().first().textContent()
            default shouldBe explicit
        }
    }
})
