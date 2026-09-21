package io.github.hiroaki404.gentrans.core.strategy

import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import io.github.hiroaki404.gentrans.core.api.InputFormat
import io.github.hiroaki404.gentrans.core.api.TranslationEvent
import io.github.hiroaki404.gentrans.core.markdown.MarkdownProtector
import io.github.hiroaki404.gentrans.core.model.LanguagePromptArgs
import io.github.hiroaki404.gentrans.core.prompt.decideTargetLanguagePrompt
import io.github.hiroaki404.gentrans.core.prompt.detectSourceLanguagePrompt
import io.github.hiroaki404.gentrans.core.prompt.refineSummaryPrompt
import io.github.hiroaki404.gentrans.core.prompt.summaryPrompt
import io.github.hiroaki404.gentrans.core.prompt.translatePrompt
import io.github.hiroaki404.gentrans.core.utility.splitForTranslation

internal data class TranslationState(
    val inputTexts: List<String> = emptyList(),
    val outputTexts: List<String> = emptyList(),
    val summarizedIntermediateTexts: List<String> = emptyList(),
    val sourceLanguage: String? = null,
    val targetLanguage: String? = null,
    /** Original blocks protected out of Markdown input by [MarkdownProtector.protect]. Unused for [InputFormat.PLAIN_TEXT]. */
    val markdownBlocks: List<String> = emptyList(),
)

/** The separator [ChunkTranslated.translatedTextSoFar][TranslationEvent.ChunkTranslated] and the final result are joined with. */
private fun chunkSeparator(inputFormat: InputFormat): String =
    if (inputFormat == InputFormat.MARKDOWN) "\n\n" else "\n"

/**
 * [onEvent] notifies the caller of each node's progress. Defaults to a no-op.
 * [io.github.hiroaki404.gentrans.core.api.Translator] converts this into a `Flow<TranslationEvent>`.
 */
internal fun createTranslationStrategy(
    languagePromptArgs: LanguagePromptArgs,
    shouldSummary: Boolean,
    inputFormat: InputFormat = InputFormat.PLAIN_TEXT,
    onEvent: suspend (TranslationEvent) -> Unit = {}
) = strategy<String, String>("GenTrans Strategy") {
    val detectSourceLanguage by node<String, TranslationState>("Detect Source Language") { input ->
        val state = llm.writeSession {
            val chunks = splitForTranslation(input, inputFormat)
            val inputTexts = chunks.inputTexts
            val markdownBlocks = chunks.markdownBlocks
            appendPrompt {
                // Skip placeholder-only chunks (e.g. a lone front matter block) so language
                // detection sees actual natural-language content.
                val detectionSample = inputTexts.firstOrNull { !MarkdownProtector.isPlaceholderOnly(it) }
                    ?: inputTexts.first()
                detectSourceLanguagePrompt(detectionSample)
            }

            val sourceLanguage = requestLLMWithoutTools().textContent().trim()
            TranslationState(
                inputTexts = inputTexts,
                markdownBlocks = markdownBlocks,
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
        val currentChunk = state.inputTexts.first()

        val nextState = if (inputFormat == InputFormat.MARKDOWN && MarkdownProtector.isPlaceholderOnly(currentChunk)) {
            // Nothing to translate (e.g. a lone front matter or fenced-code placeholder): skip
            // the LLM call and just restore this chunk's block back into it.
            val restoredText = MarkdownProtector.restore(
                currentChunk,
                state.markdownBlocks,
                MarkdownProtector.referencedBlockIndices(currentChunk)
            )
            state.copy(inputTexts = state.inputTexts.drop(1), outputTexts = state.outputTexts + restoredText)
        } else {
            llm.writeSession {
                clearHistory()
                appendPrompt {
                    translatePrompt(state.sourceLanguage, state.targetLanguage, currentChunk, inputFormat)
                }
                val translatedText = requestLLMWithoutTools().textContent().removeSuffix("\n")
                val restoredText = if (inputFormat == InputFormat.MARKDOWN) {
                    MarkdownProtector.restore(
                        translatedText,
                        state.markdownBlocks,
                        MarkdownProtector.referencedBlockIndices(currentChunk)
                    )
                } else {
                    translatedText
                }
                state.copy(inputTexts = state.inputTexts.drop(1), outputTexts = state.outputTexts + restoredText)
            }
        }
        onEvent(
            TranslationEvent.ChunkTranslated(
                chunk = nextState.outputTexts.last(),
                translatedTextSoFar = nextState.outputTexts.joinToString(chunkSeparator(inputFormat)),
                translatedChunks = nextState.outputTexts.size,
                totalChunks = totalChunks
            )
        )
        nextState
    }

    val finalizeTranslation by node<TranslationState, String>("Finalize Translation") { state ->
        state.outputTexts.joinToString(chunkSeparator(inputFormat))
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
