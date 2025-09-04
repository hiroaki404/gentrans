package org.example

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.prompt.dsl.PromptBuilder
import model.LanguagePromptArgs
import utility.splitTextByLinesWithinSize

data class TranslationState(
    val inputTexts: List<String> = emptyList(),
    val outputTexts: List<String> = emptyList(),
    val sourceLanguage: String? = null,
    val targetLanguage: String? = null
)

fun PromptBuilder.detectSourceLanguagePrompt(
    text: String
) {
    system(
        """
You are a language detection expert. Your task is to identify the natural language of given text.

Rules:
- Output the language name in English (e.g., "Japanese", "English", "French")
- Return ONLY the language name
- Do not include any other phrases or explanations
        """.trimIndent()
    )
    user("Identify the natural language of the following text:\n\n$text")
}

fun PromptBuilder.decideTargetLanguagePrompt(
    sourceLanguage: String,
    languagePromptArgs: LanguagePromptArgs
) {
    if (languagePromptArgs.targetLanguage != null) {
        system(
            """
You are a language name standardization expert. Your task is to convert language identifiers to standard English language names.

Rules:
- Convert language codes/names to standard English format
- Examples: "ja" → "Japanese", "英語" → "English", "fr" → "French"
- Return ONLY the standardized language name
- Do not include any other phrases or explanations
            """.trimIndent()
        )
        user("Convert this language identifier to standard English: ${languagePromptArgs.targetLanguage}")
    } else {
        system(
            """
Your task is to determine the appropriate target language based on user preferences.

Rules:
- Return the target language name in standard English format
- Examples: "ja" → "Japanese", "英語" → "English", "fr" → "French"
- Return ONLY the language name
- Do not include any other phrases or explanations
            """.trimIndent()
        )
        user(
            """
Determine the target language for translation and return it based on the context and logic.
            
Context:
- Input Text Language: $sourceLanguage
- Native Language: ${languagePromptArgs.nativeLanguage}
- Second Language: ${languagePromptArgs.secondLanguage}
Note: The context can contain various formats. Examples: "English", "en", "日本語", "ja"

Translation Logic:
- Input Text Language = Native Language → Second Language
- Input Text Language = Second Language → Native Language
- Input Text Language = any other language → Native Language
            """.trimMargin()
        )
    }
}

fun PromptBuilder.translatePrompt(
    sourceLanguage: String?,
    targetLanguage: String?,
    text: String
) {
    system(
        """
You are a professional translator. Your task is to translate text accurately from one language to another.

Rules:
- Provide only the translated text
- Do not include any explanations or additional phrases
        """.trimIndent()
    )

    user("Translate the following text from $sourceLanguage to $targetLanguage:\n\n$text")
}

fun createTranslationStrategy(
    languagePromptArgs: LanguagePromptArgs
) = strategy<String, String>("GenTrans Strategy") {
    val detectSourceLanguage by node<String, TranslationState>("Detect Source Language") { input ->
        llm.writeSession {
            val inputTexts = splitTextByLinesWithinSize(input, 2000)
            updatePrompt {
                detectSourceLanguagePrompt(inputTexts.first())
            }

            val sourceLanguage = requestLLMWithoutTools().content.trim()
            TranslationState(
                inputTexts = inputTexts,
                sourceLanguage = sourceLanguage,
                targetLanguage = languagePromptArgs.targetLanguage
            )
        }
    }

    val decideTargetLanguage by node<TranslationState, TranslationState>("Decide Target Language") { state ->
        llm.writeSession {
            updatePrompt {
                decideTargetLanguagePrompt(state.sourceLanguage!!, languagePromptArgs)
            }

            val targetLanguage = requestLLMWithoutTools().content.trim()
            state.copy(targetLanguage = targetLanguage)
        }
    }

    val translateByLLM by node<TranslationState, TranslationState>("Translate by LLM") { state ->
        llm.writeSession {
            updatePrompt {
                translatePrompt(state.sourceLanguage, state.targetLanguage, state.inputTexts.first())
            }
            val translatedText = requestLLMWithoutTools().content.removeSuffix("\n")
            state.copy(
                inputTexts = state.inputTexts.drop(1),
                outputTexts = state.outputTexts + translatedText
            )
        }
    }

    val finalizeTranslation by node<TranslationState, String>("Finalize Translation") { state ->
        state.outputTexts.joinToString("\n")
    }

    nodeStart then detectSourceLanguage then decideTargetLanguage then translateByLLM
    edge(translateByLLM forwardTo finalizeTranslation onCondition { it.inputTexts.isEmpty() })
    edge(translateByLLM forwardTo translateByLLM onCondition { it.inputTexts.isNotEmpty() })
    edge(finalizeTranslation forwardTo nodeFinish)
}
