package io.github.hiroaki404.gentrans.core.strategy

import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import io.github.hiroaki404.gentrans.core.api.TranslationEvent
import io.github.hiroaki404.gentrans.core.model.LanguagePromptArgs
import io.github.hiroaki404.gentrans.core.prompt.decideTargetLanguagePrompt
import io.github.hiroaki404.gentrans.core.prompt.detectSourceLanguagePrompt
import io.github.hiroaki404.gentrans.core.prompt.refineSummaryPrompt
import io.github.hiroaki404.gentrans.core.prompt.summaryPrompt
import io.github.hiroaki404.gentrans.core.prompt.translatePrompt
import io.github.hiroaki404.gentrans.core.utility.splitTextByLinesWithinSize

internal data class TranslationState(
    val inputTexts: List<String> = emptyList(),
    val outputTexts: List<String> = emptyList(),
    val summarizedIntermediateTexts: List<String> = emptyList(),
    val sourceLanguage: String? = null,
    val targetLanguage: String? = null
)

/**
 * [onEvent] notifies the caller of each node's progress. Defaults to a no-op.
 * [io.github.hiroaki404.gentrans.core.api.Translator] converts this into a `Flow<TranslationEvent>`.
 */
internal fun createTranslationStrategy(
    languagePromptArgs: LanguagePromptArgs,
    shouldSummary: Boolean,
    onEvent: suspend (TranslationEvent) -> Unit = {}
) = strategy<String, String>("GenTrans Strategy") {
    val detectSourceLanguage by node<String, TranslationState>("Detect Source Language") { input ->
        val state = llm.writeSession {
            val inputTexts = splitTextByLinesWithinSize(input, 10_000)
            appendPrompt {
                detectSourceLanguagePrompt(inputTexts.first())
            }

            val sourceLanguage = requestLLMWithoutTools().textContent().trim()
            TranslationState(
                inputTexts = inputTexts,
                sourceLanguage = sourceLanguage,
                targetLanguage = languagePromptArgs.targetLanguage
            )
        }
        onEvent(
            TranslationEvent.SourceLanguageDetected(
                language = state.sourceLanguage!!,
                totalChunks = state.inputTexts.size
            )
        )
        state
    }

    val decideTargetLanguage by node<TranslationState, TranslationState>("Decide Target Language") { state ->
        val nextState = llm.writeSession {
            appendPrompt {
                decideTargetLanguagePrompt(state.sourceLanguage!!, languagePromptArgs)
            }

            val targetLanguage = requestLLMWithoutTools().textContent().trim()
            state.copy(targetLanguage = targetLanguage)
        }
        onEvent(TranslationEvent.TargetLanguageDecided(nextState.targetLanguage!!))
        nextState
    }

    val summaryByLLM by node<TranslationState, TranslationState>("Summary by LLM") { state ->
        if (state.inputTexts.isEmpty()) {
            return@node state
        }

        val nextState = llm.writeSession {
            clearHistory()

            val currentChunk = state.inputTexts.first()
            val summarizedText = if (state.summarizedIntermediateTexts.isEmpty()) {
                appendPrompt {
                    summaryPrompt(currentChunk)
                }
                requestLLMWithoutTools().textContent().removeSuffix("\n")
            } else {
                // use refine approach https://note.com/izai/n/n23698de159c5
                val previousSummary = state.summarizedIntermediateTexts.last()
                appendPrompt {
                    refineSummaryPrompt(previousSummary, currentChunk)
                }
                val refinedSummary = requestLLMWithoutTools().textContent().removeSuffix("\n")
                refinedSummary
            }

            state.copy(
                inputTexts = state.inputTexts.drop(1),
                summarizedIntermediateTexts = listOf(summarizedText)
            )
        }
        onEvent(TranslationEvent.Summarized(remainingChunks = nextState.inputTexts.size))
        nextState
    }

    val finalizeSummary by node<TranslationState, TranslationState>("Finalize Summary") { state ->
        state.copy(
            inputTexts = state.summarizedIntermediateTexts,
            summarizedIntermediateTexts = emptyList()
        )
    }

    val translateByLLM by node<TranslationState, TranslationState>("Translate by LLM") { state ->
        // This node loops on itself until the remaining chunks run out, so the total chunk
        // count used for notification is rebuilt each time from "translated so far + this one +
        // remaining" (after summarizing, the total becomes 1).
        val totalChunks = state.outputTexts.size + state.inputTexts.size
        val nextState = llm.writeSession {
            clearHistory()
            appendPrompt {
                translatePrompt(state.sourceLanguage, state.targetLanguage, state.inputTexts.first())
            }
            val translatedText = requestLLMWithoutTools().textContent().removeSuffix("\n")
            state.copy(
                inputTexts = state.inputTexts.drop(1),
                outputTexts = state.outputTexts + translatedText
            )
        }
        onEvent(
            TranslationEvent.ChunkTranslated(
                chunk = nextState.outputTexts.last(),
                translatedTextSoFar = nextState.outputTexts.joinToString("\n"),
                translatedChunks = nextState.outputTexts.size,
                totalChunks = totalChunks
            )
        )
        nextState
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
