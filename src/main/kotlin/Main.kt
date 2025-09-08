package org.example

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.features.opentelemetry.feature.OpenTelemetry
import ai.koog.agents.features.opentelemetry.integration.langfuse.addLangfuseExporter
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
import domain.GetLanguagePromptArgsUseCase
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
        help = "AI model to use. e.g. `gemini-2.0-flash`, `gpt-4o`, `claude-3-opus`, `gemma3n:latest`, `llama3.2:latest`. Supported models depend on the Koog library. See documentation for details."
    )
    private val targetLanguage: String? by option(
        names = arrayOf("-t", "--to"),
        help = "Specify the target language. Since the language is interpreted by an LLM, you can use various formats like `English`, `en`, or even `日本語`."
    )

    private val targetText: List<String> by argument(help = "Text to translate. Reads from stdin if not provided.").multiple()

    val getLLModelUseCase: GetLLModelUseCase = GetLLModelUseCase()
    private val getLanguagePromptArgsUseCase: GetLanguagePromptArgsUseCase = GetLanguagePromptArgsUseCase()

    override suspend fun run() {
        if (BuildInfo.IS_DEBUG) {
            echo("DEBUG: GenTrans v${BuildInfo.VERSION}")
        }

        val text = if (targetText.isNotEmpty()) {
            targetText.joinToString("\n")
        } else {
            generateSequence(::readlnOrNull).joinToString("\n")
        }

        val executor = getExecutor(provider, apikey)
        val llmModel = getLLModelUseCase(model, provider)
        val languagePromptArgs = getLanguagePromptArgsUseCase(targetLanguage)

        val strategy = createTranslationStrategy(languagePromptArgs)

        val agent = AIAgent(
            executor = executor,
            llmModel = llmModel,
            strategy = strategy,
        ) {
            install(OpenTelemetry) {
                setVerbose(true)
                addLangfuseExporter()
            }
        }

        val result = agent.run(text)
        echo(result)
    }
}

suspend fun main(args: Array<String>) = GenTransCommand().main(args)
