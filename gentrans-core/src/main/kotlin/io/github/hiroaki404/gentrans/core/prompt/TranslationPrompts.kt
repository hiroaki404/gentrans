package io.github.hiroaki404.gentrans.core.prompt

import ai.koog.prompt.dsl.PromptBuilder
import io.github.hiroaki404.gentrans.core.api.InputFormat

internal fun PromptBuilder.translatePrompt(
    sourceLanguage: String?,
    targetLanguage: String?,
    text: String,
    inputFormat: InputFormat = InputFormat.PLAIN_TEXT
) {
    val baseSystemPrompt = """
You are a professional translator. Your task is to translate text accurately from one language to another.

Rules:
- Provide only the translated text
- Do not include any explanations or additional phrases
    """.trimIndent()

    val markdownSystemPrompt = """
Additional rules for Markdown input:
- Preserve all Markdown syntax exactly (headings, lists, tables, emphasis, links)
- Do not translate the contents of URLs, image paths, or inline code spans
- Keep every `GENTRANS_BLOCK_<number>` token exactly as it is, character for character
    """.trimIndent()

    system(
        if (inputFormat == InputFormat.MARKDOWN) {
            "$baseSystemPrompt\n\n$markdownSystemPrompt"
        } else {
            baseSystemPrompt
        }
    )

    user(
        """
Translate the following text from $sourceLanguage to $targetLanguage:

---

$text
        """.trimIndent()
    )
}
