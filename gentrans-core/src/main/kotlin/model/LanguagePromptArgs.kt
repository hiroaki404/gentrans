package model

data class LanguagePromptArgs(
    val targetLanguage: String?,
    val nativeLanguage: String,
    val secondLanguage: String
)
