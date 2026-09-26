package io.github.hiroaki404.gentrans.core.utility

import io.github.hiroaki404.gentrans.core.api.InputFormat
import io.github.hiroaki404.gentrans.core.markdown.MarkdownProtector
import io.github.hiroaki404.gentrans.core.markdown.MarkdownSegmenter

internal const val MAX_CHUNK_SIZE = 10_000

internal data class TranslationChunks(
    val inputTexts: List<String>,
    val markdownBlocks: List<String>,
)

internal fun splitForTranslation(
    text: String,
    format: InputFormat,
    maxChunkSize: Int = MAX_CHUNK_SIZE,
): TranslationChunks =
    if (format == InputFormat.MARKDOWN) {
        val protected = MarkdownProtector.protect(text)
        TranslationChunks(MarkdownSegmenter.split(protected.text, maxChunkSize), protected.blocks)
    } else {
        TranslationChunks(splitTextByLinesWithinSize(text, maxChunkSize), emptyList())
    }

public fun splitTextByLinesWithinSize(text: String, maxSize: Int): List<String> {
    if (text.isEmpty()) return emptyList()
    if (text.length <= maxSize) return listOf(text)

    return text.split("\n")
        .fold(mutableListOf<String>() to "") { (result, currentChunk), line ->
            val testChunk = if (currentChunk.isEmpty()) line else "$currentChunk\n$line"

            when {
                testChunk.length <= maxSize -> result to testChunk
                else -> {
                    if (currentChunk.isNotEmpty()) result.add(currentChunk)
                    result to line
                }
            }
        }
        .let { (result, lastChunk) ->
            if (lastChunk.isNotEmpty()) result.add(lastChunk)
            result
        }
}
