package prompt

import ai.koog.prompt.dsl.PromptBuilder

fun PromptBuilder.summaryPrompt(
    text: String,
) {
    system(
        """
You are an assistant who summarizes long texts.
        """.trimIndent()
    )
    user(
        """
Please concisely summarize the following text, including all the important points. Appropriately combine bullet points and text to create a logical structure. Please summarize the text in an easy-to-understand manner. Keep output within about 800 characters. Please maintain the language of the given text.
Summarize the following text:

---

$text
        """.trimIndent()
    )
}

fun PromptBuilder.refineSummaryPrompt(
    previousSummary: String,
    newChunk: String
) {
    system(
        """
You are an assistant who refines and improves summaries by incorporating new information.
        """.trimIndent()
    )
    user(
        """
I have an existing summary and new text to add. Please create an improved, integrated summary that:
1. Incorporates all important information from both the existing summary and new text
2. Maintains consistency and coherence
3. Keeps the refined summary within about 800 characters
4. Uses bullet points and concise sentences as needed for clarity
5. Please maintain the language of the given text.

Existing Summary:
---
$previousSummary

New Text to Integrate:
---
$newChunk

Please provide the refined, integrated summary:
        """.trimIndent()
    )
}
