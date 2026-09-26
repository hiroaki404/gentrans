package io.github.hiroaki404.gentrans.cli

import ai.koog.agents.features.opentelemetry.feature.OpenTelemetry
import ai.koog.agents.features.opentelemetry.integration.langfuse.addLangfuseExporter
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.choice
import io.github.hiroaki404.gentrans.cli.config.ResolveTranslatorConfigUseCase
import io.github.hiroaki404.gentrans.core.api.InputFormat
import io.github.hiroaki404.gentrans.core.api.InternalGentransApi
import io.github.hiroaki404.gentrans.core.api.TranslationRequest
import io.github.hiroaki404.gentrans.core.api.Translator
import io.github.hiroaki404.gentrans.core.api.TranslatorConfig
import io.github.hiroaki404.gentrans.core.api.createTranslator
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration

class GenTransCommand(
    private val translatorFactory: (TranslatorConfig) -> Translator = { config ->
        @OptIn(InternalGentransApi::class)
        createTranslator(config)
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
    private val enableTrace: Boolean by option(
        names = arrayOf("--trace"),
        help = "Enable OpenTelemetry tracing.",
        hidden = !BuildConfig.IS_DEBUG
    ).flag()

    private val shouldSummary: Boolean by option(
        names = arrayOf("-s", "--summary"),
        help = "Enable text summarization before translation. Useful for long texts."
    ).flag()

    private val inputFormat: InputFormat by option(
        names = arrayOf("-m", "--format"),
        help = "Input text format. `plain` (default) or `markdown`. With `markdown`, fenced code blocks and front matter are kept verbatim and Markdown syntax is preserved."
    ).choice("plain" to InputFormat.PLAIN_TEXT, "markdown" to InputFormat.MARKDOWN)
        .default(InputFormat.PLAIN_TEXT)

    private val targetText: List<String> by argument(help = "Text to translate. Reads from stdin if not provided.").multiple()

    private val resolveTranslatorConfigUseCase = ResolveTranslatorConfigUseCase()

    override suspend fun run() {
        val text = if (targetText.isNotEmpty()) {
            targetText.joinToString("\n")
        } else {
            generateSequence(::readlnOrNull).joinToString("\n")
        }

        val config = resolveTranslatorConfigUseCase(
            providerOption = provider,
            modelOption = model,
            apiKeyOption = apikey,
        )

        val translator = if (BuildConfig.IS_DEBUG && enableTrace) {
            @OptIn(InternalGentransApi::class)
            createTranslator(config) {
                install(OpenTelemetry) {
                    setVerbose(true)
                    addLangfuseExporter()
                }
            }
        } else {
            translatorFactory(config)
        }

        val result = translator.translateToText(
            TranslationRequest(
                text = text,
                targetLanguage = targetLanguage,
                shouldSummary = shouldSummary,
                inputFormat = inputFormat,
            )
        )
        echo(result)
    }
}

internal fun suppressKotlinLoggingStartupMessage() {
    KotlinLoggingConfiguration.logStartupMessage = false
}

suspend fun main(args: Array<String>) {
    suppressKotlinLoggingStartupMessage()
    GenTransCommand().main(args)
}
