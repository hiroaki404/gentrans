package org.example

import ai.koog.agents.core.dsl.builder.strategy
import model.LanguagePromptArgs

fun detectSourceLanguagePrompt(text: String) = """
# Instructions:

Please identify the natural language of the following text. Output the language name in English. For example, "Japanese", "English", "French", etc.
Return ONLY the language name. Do not include any other phrases or explanations.

# Text to analyze:

$text
""".trimIndent()

fun decideTargetLanguagePrompt(
    languagePromptArgs: LanguagePromptArgs
) = """Please determine the target natural language for translation according to the following rules.
${
    languagePromptArgs.targetLanguage?.let {
        """Target language: $it
This is the target natural language, but please replace it with the English natural language name.
For example, "ja" should be "Japanese", "英語" should be "English", and so on.
Return ONLY the language name. Do not include any other phrases or explanations.
"""
    } ?: """
Native Language: ${languagePromptArgs.nativeLanguage}, Second Language: ${languagePromptArgs.secondLanguage}
    - If the input text is in Native Language, translate to Second Language
    - If the input text is in Second Language, translate to Native Language  
    - If the input text is in any other language, translate to Native Language
The language determined by this rule is the target natural language, but please replace it with the English natural language name.
For example, "ja" should be "Japanese", "英語" should be "English", and so on.
Return ONLY the language name. Do not include any other phrases or explanations.
"""
}
""".trimIndent()

fun translatePrompt(
    sourceLanguage: String?,
    targetLanguageVar: String?,
    targetText: String
) = """
Translate the following text, from $sourceLanguage to $targetLanguageVar.
Return ONLY the translated text. Do not include any other phrases or explanations.
---
$targetText.
""".trimIndent()

fun createTranslationStrategy(
    languagePromptArgs: LanguagePromptArgs
) = strategy<String, String>("GenTrans Strategy") {
    var sourceLanguage: String? = null
    var targetLanguageVar: String? = languagePromptArgs.targetLanguage
    var inputText = ""

    val detectSourceLanguage by node<String, String>("Detect Source Language") { input ->
        llm.writeSession {
            inputText = input
            updatePrompt {
                user(detectSourceLanguagePrompt(inputText))
            }

            requestLLMWithoutTools().content.also {
                sourceLanguage = it.trim()
            }
        }
    }

    val decideTargetLanguage by node<String, String>("Decide Target Language") {
        llm.writeSession {
            updatePrompt {
                user(decideTargetLanguagePrompt(languagePromptArgs))
            }

            requestLLMWithoutTools().content.also {
                targetLanguageVar = it.trim()
            }
        }
    }

    val translateByLLM by node<String, String>("Translate to LLM") {
        llm.writeSession {
            updatePrompt {
                user(translatePrompt(sourceLanguage, targetLanguageVar, inputText))
            }
            requestLLMWithoutTools().content.removeSuffix("\n")
        }
    }

    nodeStart then detectSourceLanguage then decideTargetLanguage then translateByLLM then nodeFinish
}
