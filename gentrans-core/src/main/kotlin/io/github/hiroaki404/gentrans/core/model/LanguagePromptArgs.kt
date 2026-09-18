package io.github.hiroaki404.gentrans.core.model

internal data class LanguagePromptArgs(
    val targetLanguage: String?,
    val nativeLanguage: String,
    val secondLanguage: String
)
