package io.github.hiroaki404.gentrans.plugin.settings

import io.github.hiroaki404.gentrans.core.api.Provider
import io.github.hiroaki404.gentrans.core.api.TranslatorConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** What [GentransSettings.State] plus the stored API key resolve to for a translation. */
internal data class ResolvedGentransConfig(
    val translatorConfig: TranslatorConfig,
    val targetLanguage: String?,
)

/** Ollama needs no key, so it's never looked up for that provider even if one is stored. */
internal suspend fun GentransSettings.State.resolveConfig(apiKeyStore: ApiKeyStore): ResolvedGentransConfig {
    val resolvedProvider = resolveSupportedProviderOrDefault(provider)
    val apiKey = if (resolvedProvider == Provider.Ollama) {
        null
    } else {
        withContext(Dispatchers.IO) { apiKeyStore.get(resolvedProvider) }
    }
    return ResolvedGentransConfig(
        translatorConfig = TranslatorConfig(
            provider = resolvedProvider,
            model = model,
            apiKey = apiKey,
            nativeLanguage = nativeLanguage,
            secondLanguage = secondLanguage,
            ollamaBaseUrl = ollamaBaseUrl.ifBlank { TranslatorConfig.DEFAULT_OLLAMA_BASE_URL },
        ),
        targetLanguage = targetLanguage.ifBlank { null },
    )
}
