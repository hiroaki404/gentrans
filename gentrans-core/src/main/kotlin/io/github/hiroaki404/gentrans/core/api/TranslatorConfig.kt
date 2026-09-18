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
)
