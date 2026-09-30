package io.github.hiroaki404.gentrans.plugin.translation

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportSequentialProgress
import io.github.hiroaki404.gentrans.core.api.InputFormat
import io.github.hiroaki404.gentrans.core.api.Provider
import io.github.hiroaki404.gentrans.core.api.TranslationEvent
import io.github.hiroaki404.gentrans.core.api.TranslationRequest
import io.github.hiroaki404.gentrans.core.api.Translator
import io.github.hiroaki404.gentrans.core.api.TranslatorConfig
import io.github.hiroaki404.gentrans.plugin.GentransBundle
import io.github.hiroaki404.gentrans.plugin.settings.ApiKeyStore
import io.github.hiroaki404.gentrans.plugin.settings.GentransSettings
import io.github.hiroaki404.gentrans.plugin.settings.PasswordSafeApiKeyStore
import io.github.hiroaki404.gentrans.plugin.settings.resolveConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong

internal sealed interface TranslationFailure {
    data class ApiKeyMissing(val provider: Provider) : TranslationFailure
    data class Generic(val message: String) : TranslationFailure
}

internal data class TranslationOrigin(val provider: String, val model: String)

@Service(Service.Level.PROJECT)
internal class TranslationService @JvmOverloads constructor(
    private val project: Project,
    private val scope: CoroutineScope,
    private val translatorFactory: (TranslatorConfig) -> Translator = ::Translator,
    private val apiKeyStore: ApiKeyStore = PasswordSafeApiKeyStore(),
) {
    private val generation = AtomicLong()
    private var currentJob: Job? = null

    fun translate(
        text: String,
        onEvent: (TranslationEvent) -> Unit,
        onFailure: (TranslationFailure) -> Unit,
        onCompletedOrigin: (TranslationOrigin) -> Unit = {},
    ) {
        currentJob?.cancel()
        val currentGeneration = generation.incrementAndGet()
        currentJob = scope.launch {
            var resolvedProvider: Provider? = null
            try {
                withBackgroundProgress(project, GentransBundle.message("gentrans.progress.title"), true) {
                    val resolved = GentransSettings.getInstance().state.resolveConfig(apiKeyStore)
                    resolvedProvider = resolved.translatorConfig.provider
                    reportSequentialProgress { reporter ->
                        translatorFactory(resolved.translatorConfig).translate(
                            TranslationRequest(text, resolved.targetLanguage, false, InputFormat.MARKDOWN),
                        ).collect { event ->
                            when (event) {
                                is TranslationEvent.SourceLanguageDetected ->
                                    reporter.indeterminateStep(GentransBundle.message("gentrans.progress.detecting", event.language))
                                is TranslationEvent.TargetLanguageDecided ->
                                    reporter.indeterminateStep(GentransBundle.message("gentrans.progress.deciding", event.language))
                                is TranslationEvent.ChunkTranslated ->
                                    reporter.indeterminateStep(GentransBundle.message("gentrans.progress.translating", event.translatedChunks, event.totalChunks))
                                else -> Unit
                            }
                            if (generation.get() == currentGeneration) {
                                withContext(Dispatchers.EDT) {
                                    if (generation.get() == currentGeneration) {
                                        if (event is TranslationEvent.Completed) {
                                            onCompletedOrigin(TranslationOrigin(
                                                resolved.translatorConfig.provider.key,
                                                resolved.translatorConfig.model,
                                            ))
                                        }
                                        onEvent(event)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                LOG.warn("Translation failed", error)
                val provider = resolvedProvider
                val failure = if (error is IllegalArgumentException && error.message?.endsWith("API key is required") == true && provider != null) {
                    TranslationFailure.ApiKeyMissing(provider)
                } else {
                    TranslationFailure.Generic(error.message ?: error.javaClass.simpleName)
                }
                withContext(NonCancellable + Dispatchers.EDT) {
                    if (generation.get() == currentGeneration) onFailure(failure)
                }
            }
        }
    }

    fun cancel() {
        generation.incrementAndGet()
        currentJob?.cancel()
    }

    companion object {
        private val LOG = Logger.getInstance(TranslationService::class.java)

        fun getInstance(project: Project): TranslationService = project.service()
    }
}
