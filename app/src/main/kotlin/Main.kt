package org.example

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.versionOption
import org.example.app.BuildConfig

class GenTransCommand(
    private val executor: PromptExecutor = simpleGoogleAIExecutor(System.getenv("GOOGLE_API_KEY"))
) : SuspendingCliktCommand() {
    init {
        versionOption(BuildConfig.VERSION)
    }

    private val targetText: List<String> by argument(help = "Text to translate. Reads from stdin if not provided.").multiple()

    override suspend fun run() {
        val text = if (targetText.isNotEmpty()) {
            targetText.joinToString("\n")
        } else {
            generateSequence(::readlnOrNull).joinToString("\n")
        }

        val agent = AIAgent(
            executor = executor,
            llmModel = GoogleModels.Gemini2_0Flash
        )
        AnthropicModels.Sonnet_4
        val prompt = """
            Translate the following text into English.
            - Return only the translated text.
            - If the text is already in English, return the original text.
        """.trimIndent()
        val result = agent.runAndGetResult("$prompt:\n$text")
        echo(result)
    }
}

suspend fun main(args: Array<String>) = GenTransCommand().main(args)
