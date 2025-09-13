package prompt

import ai.koog.prompt.dsl.PromptBuilder
import model.LanguagePromptArgs

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
    user(
        """
Identify the natural language of the following text:

---

$text
        """.trimIndent()
    )
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