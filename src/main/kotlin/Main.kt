package org.example

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import domain.GetExecutorUseCase
import domain.GetLLModelUseCase
import org.example.gentrans.BuildConfig

class GenTransCommand(
    private val getExecutor: (providerOption: String?, apikey: String?) -> PromptExecutor = { providerOption, apikey ->
        val getExecutorUseCase = GetExecutorUseCase()
        SingleLLMPromptExecutor(getExecutorUseCase(providerOption, apikey))
    }
) : SuspendingCliktCommand() {
    init {
        versionOption(BuildConfig.VERSION)
    }

    private val apikey: String? by option(help = "API key for the AI provider.")
    private val provider: String? by option(
        help = "AI provider to use. Supported providers are `google`, `openai`, `anthropic`, `meta`, `alibaba`, `openrouter`, and `ollama`."
    )
    private val model: String? by option(
        help = "AI model to use. e.g. `gemini-2.0-flash`, `gpt-4o`, `claude-3-opus`. Supported models depend on the Koog library. See documentation for details."
    )
    private val to: String? by option(
        names = arrayOf("-t", "--to"),
        help = "Specify the target language. Since the language is interpreted by an LLM, you can use various formats like `English`, `en`, or even `日本語`."
    )

    private val targetText: List<String> by argument(help = "Text to translate. Reads from stdin if not provided.").multiple()

    val getLLModelUseCase: GetLLModelUseCase = GetLLModelUseCase()

    override suspend fun run() {
        val text = if (targetText.isNotEmpty()) {
            targetText.joinToString("\n")
        } else {
            generateSequence(::readlnOrNull).joinToString("\n")
        }

        val executor = getExecutor(provider, apikey)
        val llmModel = getLLModelUseCase(model, provider)

        val agent = AIAgent(
            executor = executor,
            llmModel = llmModel
        )
        val prompt = """
# Instruction
Translate the following text to the target language.

# Target Language
${to ?: "English"}
(Note: The target language may be specified by a name, a language code like 'ja', or a name in its native script like '日本語'.)

# Rules
- Return ONLY the translated text. Do not include any other phrases or explanations.
- If the source text is already in the target language, return the original text without modification.

---
""".trimIndent()
        val result = agent.run("$prompt:\n$text")
        echo(result)
    }
}

suspend fun main(args: Array<String>) = GenTransCommand().main(args)
