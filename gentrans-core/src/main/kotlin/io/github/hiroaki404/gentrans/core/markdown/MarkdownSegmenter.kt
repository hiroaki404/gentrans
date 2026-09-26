package io.github.hiroaki404.gentrans.core.markdown

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser

/**
 * Splits markdown into chunks without breaking top-level block structures (paragraphs,
 * headings, lists, blockquotes, tables, fenced code, HTML blocks, …) across a chunk boundary.
 *
 * Intended to run after [MarkdownProtector.protect], so the input is already placeholder-safe;
 * this object has no special knowledge of placeholders and treats them like any other text.
 */
internal object MarkdownSegmenter {

    private val NON_BLOCK_TOKEN_TYPES = setOf(MarkdownTokenTypes.EOL, MarkdownTokenTypes.WHITE_SPACE)

    /**
     * Splits [markdown] into chunks of at most [maxSize] characters each, by greedily grouping
     * consecutive top-level blocks in document order. Each chunk is the exact source slice from
     * the start of its first block to the end of its last block, so whitespace between blocks
     * inside a chunk is preserved verbatim, while whitespace around and between chunks is
     * dropped; callers are expected to rejoin chunks with their own separator.
     *
     * A single block that alone exceeds [maxSize] is never split further and becomes its own
     * chunk, since splitting it (e.g. mid-table or mid-list) would break its structure.
     *
     * Returns an empty list when [markdown] is blank.
     */
    fun split(markdown: String, maxSize: Int): List<String> {
        val blockRanges = findTopLevelBlockRanges(markdown)
        if (blockRanges.isEmpty()) return emptyList()

        val chunkRanges = mutableListOf<IntRange>()
        var groupStart = blockRanges.first().first
        var groupEnd = blockRanges.first().last

        for (range in blockRanges.drop(1)) {
            if (range.last - groupStart + 1 <= maxSize) {
                groupEnd = range.last
            } else {
                chunkRanges += groupStart..groupEnd
                groupStart = range.first
                groupEnd = range.last
            }
        }
        chunkRanges += groupStart..groupEnd

        return chunkRanges.map { markdown.substring(it.first, it.last + 1) }
    }

    private fun findTopLevelBlockRanges(markdown: String): List<IntRange> {
        val parser = MarkdownParser(GFMFlavourDescriptor(), assertionsEnabled = false, cancellationToken = CancellationToken.NonCancellable)
        val tree = parser.buildMarkdownTreeFromString(markdown as CharSequence)

        return tree.children
            .filterNot { it.type in NON_BLOCK_TOKEN_TYPES }
            .map { it.startOffset until it.endOffset }
    }
}
