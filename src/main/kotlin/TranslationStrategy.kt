package org.example

import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.clearHistory
import model.LanguagePromptArgs
import prompt.decideTargetLanguagePrompt
import prompt.detectSourceLanguagePrompt
import prompt.refineSummaryPrompt
import prompt.summaryPrompt
import prompt.translatePrompt
import utility.splitTextByLinesWithinSize

data class TranslationState(
    val inputTexts: List<String> = emptyList(),
    val outputTexts: List<String> = emptyList(),
    val summarizedIntermediateTexts: List<String> = emptyList(),
    val sourceLanguage: String? = null,
    val targetLanguage: String? = null
)

fun createTranslationStrategy(
    languagePromptArgs: LanguagePromptArgs,
    shouldSummary: Boolean
) = strategy<String, String>("GenTrans Strategy") {
    val detectSourceLanguage by node<String, TranslationState>("Detect Source Language") { input ->
        llm.writeSession {
            val inputTexts = splitTextByLinesWithinSize(input, 20000)
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

    val summaryByLLM by node<TranslationState, TranslationState>("Summary by LLM") { state ->
        if (state.inputTexts.isEmpty()) {
            return@node state
        }

        llm.writeSession {
            clearHistory()

            val currentChunk = state.inputTexts.first()
            val summarizedText = if (state.summarizedIntermediateTexts.isEmpty()) {
                updatePrompt {
                    summaryPrompt(currentChunk)
                }
                requestLLMWithoutTools().content.removeSuffix("\n")
            } else {
                // use refine approach https://note.com/izai/n/n23698de159c5
                val previousSummary = state.summarizedIntermediateTexts.last()
                updatePrompt {
                    refineSummaryPrompt(previousSummary, currentChunk)
                }
                val refinedSummary = requestLLMWithoutTools().content.removeSuffix("\n")
                refinedSummary
            }

            state.copy(
                inputTexts = state.inputTexts.drop(1),
                summarizedIntermediateTexts = listOf(summarizedText)
            )
        }
    }

    val finalizeSummary by node<TranslationState, TranslationState>("Finalize Summary") { state ->
        state.copy(
            inputTexts = state.summarizedIntermediateTexts,
            summarizedIntermediateTexts = emptyList()
        )
    }

    val translateByLLM by node<TranslationState, TranslationState>("Translate by LLM") { state ->
        llm.writeSession {
            clearHistory()
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

    nodeStart then detectSourceLanguage then decideTargetLanguage

    if (shouldSummary) {
        decideTargetLanguage then summaryByLLM
        edge(summaryByLLM forwardTo finalizeSummary onCondition { it.inputTexts.isEmpty() })
        edge(summaryByLLM forwardTo summaryByLLM onCondition { it.inputTexts.isNotEmpty() })
        finalizeSummary then translateByLLM
    } else {
        decideTargetLanguage then translateByLLM
    }

    edge(translateByLLM forwardTo finalizeTranslation onCondition { it.inputTexts.isEmpty() })
    edge(translateByLLM forwardTo translateByLLM onCondition { it.inputTexts.isNotEmpty() })
    edge(finalizeTranslation forwardTo nodeFinish)
}
