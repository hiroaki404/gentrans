package io.github.hiroaki404.gentrans.core.api

/**
 * One translation request. The only argument [Translator] takes besides its config.
 */
public data class TranslationRequest(
    val text: String,
    val targetLanguage: String? = null,
    val shouldSummary: Boolean = false,
    val inputFormat: InputFormat = InputFormat.PLAIN_TEXT,
)
