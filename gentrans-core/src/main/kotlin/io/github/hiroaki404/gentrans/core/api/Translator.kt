package io.github.hiroaki404.gentrans.core.api

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.prompt.executor.model.PromptExecutor
import io.github.hiroaki404.gentrans.core.domain.buildLLMClient
import io.github.hiroaki404.gentrans.core.domain.buildLLModel
import io.github.hiroaki404.gentrans.core.model.LanguagePromptArgs
import io.github.hiroaki404.gentrans.core.strategy.createTranslationStrategy
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow

/**
 * The facade for translation. The only entry point frontends (CLI / IDE plugin) touch.
 *
 * Koog types stay out of the plain public API; use [createTranslator] with an explicit
 * [PromptExecutor] factory when Koog-level customization (e.g. tracing) is needed.
 */
public class Translator internal constructor(
    private val config: TranslatorConfig,
    private val executorFactory: (TranslatorConfig) -> PromptExecutor,
    private val installFeatures: GraphAIAgent.FeatureContext.() -> Unit,
) {
    public constructor(config: TranslatorConfig) : this(config, ::defaultExecutor, {})

    /**
     * Runs a translation, streaming progress as [TranslationEvent]. [TranslationEvent.Completed]
     * is always emitted last. Failures are thrown as exceptions from this `Flow`.
     */
    public fun translate(request: TranslationRequest): Flow<TranslationEvent> = channelFlow {
        val executor = executorFactory(config)
        val llmModel = buildLLModel(config.model, config.provider.key)
        val languagePromptArgs = LanguagePromptArgs(
            targetLanguage = request.targetLanguage,
            nativeLanguage = config.nativeLanguage,
            secondLanguage = config.secondLanguage,
        )

        val agent = AIAgent(
            promptExecutor = executor,
            llmModel = llmModel,
            strategy = createTranslationStrategy(languagePromptArgs, request.shouldSummary) { send(it) },
            installFeatures = installFeatures,
        )

        send(TranslationEvent.Completed(agent.run(request.text)))
        // Buffer so a slow subscriber doesn't stall the LLM calls themselves.
    }.buffer(Channel.BUFFERED)

    /** A thin wrapper for callers that don't need progress; returns only the final result. */
    public suspend fun translateToText(request: TranslationRequest): String {
        var result: String? = null
        translate(request).collect { event ->
            if (event is TranslationEvent.Completed) result = event.text
        }
        return requireNotNull(result) { "Translation finished without a result" }
    }
}

internal fun defaultExecutor(config: TranslatorConfig): PromptExecutor =
    PromptExecutor.builder().addClient(buildLLMClient(config.provider.key, config.apiKey)).build()

@InternalGentransApi
public fun createTranslator(
    config: TranslatorConfig,
    executorFactory: (TranslatorConfig) -> PromptExecutor = ::defaultExecutor,
    installFeatures: GraphAIAgent.FeatureContext.() -> Unit = {},
): Translator = Translator(config, executorFactory, installFeatures)
