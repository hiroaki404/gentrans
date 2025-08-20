package org.example

import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.Serializable

@Serializable
@LLMDescription("")
data class TranslateRule(
    @property:LLMDescription("")
    val sourceLanguage: String,

    @property:LLMDescription("")
    val targetLanguage: String
)

@Serializable
@LLMDescription("Extract the language used in the text from the text source.")
data class SourceLanguage(
    @property:LLMDescription("Text source language. The expression of language names should be in English notation.")
    val sourceLanguage: String
)
