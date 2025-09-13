package prompt

import ai.koog.prompt.dsl.PromptBuilder

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

    user(
        """
Translate the following text from $sourceLanguage to $targetLanguage:

---

$text
        """.trimIndent()
    )
}