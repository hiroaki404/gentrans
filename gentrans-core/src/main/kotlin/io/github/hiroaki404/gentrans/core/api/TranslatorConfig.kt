package io.github.hiroaki404.gentrans.core.api

/**
 * Fully resolved configuration a [Translator] needs. Callers are responsible for resolving
 * provider/model/api key/languages (option > local config > env > default) before constructing
 * this.
 */
public data class TranslatorConfig(
    val provider: Provider,
    val model: String,
    val apiKey: String?,
    val nativeLanguage: String,
    val secondLanguage: String,
    val ollamaBaseUrl: String = DEFAULT_OLLAMA_BASE_URL,
) {
    public companion object {
        // Matches ai.koog.prompt.executor.ollama.client.OllamaClient.DEFAULT_BASE_URL, duplicated
        // as a literal so this api-layer file stays free of Koog imports.
        public const val DEFAULT_OLLAMA_BASE_URL: String = "http://localhost:11434"
    }
}
