package io.github.hiroaki404.gentrans.core.markdown

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser

/**
 * Result of [MarkdownProtector.protect].
 *
 * @property text The markdown with protected blocks replaced by `GENTRANS_BLOCK_n` placeholders.
 * @property blocks The original block substrings, indexed by their placeholder number.
 */
internal data class ProtectedMarkdown(val text: String, val blocks: List<String>)

/**
 * Thrown by [MarkdownProtector.restore] when the placeholders in the translated text do not
 * match [ProtectedMarkdown.blocks] exactly, indicating the placeholders were tampered with
 * (removed, duplicated, or altered) during translation. No best-effort restoration is attempted.
 *
 * @property missing Block indices whose placeholder is absent or was altered (an altered
 *   placeholder, e.g. wrong case, full-width digits, or a changed separator, is treated as missing).
 * @property duplicated Block indices whose placeholder appears more than once.
 * @property unknown Placeholder indices found in the text that do not correspond to any block.
 */
internal class PlaceholderMismatchException(
    val missing: List<Int>,
    val duplicated: List<Int> = emptyList(),
    val unknown: List<Int> = emptyList(),
) : IllegalStateException(buildMismatchMessage(missing, duplicated, unknown))

private fun buildMismatchMessage(missing: List<Int>, duplicated: List<Int>, unknown: List<Int>): String {
    val parts = mutableListOf<String>()
    if (missing.isNotEmpty()) parts += "missing=$missing"
    if (duplicated.isNotEmpty()) parts += "duplicated=$duplicated"
    if (unknown.isNotEmpty()) parts += "unknown=$unknown"
    return "Markdown placeholder mismatch detected (${parts.joinToString(", ")})"
}

/**
 * Shields markdown content that must survive translation untouched by an LLM, so it can be
 * restored verbatim afterward instead of being handed to the model.
 *
 * Only the YAML front matter at the very start of a document and fenced code blocks (including
 * ones nested inside list items or blockquotes) are protected; inline code spans, URLs, and image
 * paths are left as-is by design, since the translation prompt is expected to handle those.
 *
 * Protected blocks are replaced with `GENTRANS_BLOCK_n` placeholders, numbered from 0 in document
 * order, that an LLM is unlikely to translate or otherwise alter.
 */
internal object MarkdownProtector {

    private const val PLACEHOLDER_PREFIX = "GENTRANS_BLOCK_"
    private val PLACEHOLDER_REGEX = Regex("GENTRANS_BLOCK_(\\d+)")

    /**
     * Replaces the front matter and every fenced code block in [markdown] with placeholder
     * tokens, capturing each one's exact original source substring in [ProtectedMarkdown.blocks].
     *
     * Because the original substrings are stored verbatim, `restore(protect(x).text, protect(x).blocks)`
     * always equals `x`.
     */
    fun protect(markdown: String): ProtectedMarkdown {
        val ranges = mutableListOf<IntRange>()

        val frontMatterRange = findFrontMatterRange(markdown)
        if (frontMatterRange != null) {
            ranges += frontMatterRange
        }

        val fenceRanges = findFencedCodeRanges(markdown).filterNot { fence ->
            frontMatterRange != null && fence.first <= frontMatterRange.last && frontMatterRange.first <= fence.last
        }
        ranges += fenceRanges

        val sortedRanges = ranges.sortedBy { it.first }
        val blocks = mutableListOf<String>()
        val text = buildString {
            var cursor = 0
            for (range in sortedRanges) {
                append(markdown, cursor, range.first)
                blocks += markdown.substring(range.first, range.last + 1)
                append(PLACEHOLDER_PREFIX).append(blocks.lastIndex)
                cursor = range.last + 1
            }
            append(markdown, cursor, markdown.length)
        }

        return ProtectedMarkdown(text, blocks)
    }

    /**
     * Replaces each `GENTRANS_BLOCK_n` placeholder in [translated] with its original block from
     * [blocks]. Note that any `GENTRANS_BLOCK_n`-shaped text already present in the source
     * document, and not produced by [protect], is indistinguishable from a placeholder and will
     * be treated as one here.
     *
     * @throws PlaceholderMismatchException if any placeholder is missing, altered, duplicated, or
     *   refers to an unknown block index. No best-effort restoration is attempted in that case.
     */
    fun restore(translated: String, blocks: List<String>): String {
        val occurrences = mutableMapOf<Int, Int>()
        for (match in PLACEHOLDER_REGEX.findAll(translated)) {
            val index = match.groupValues[1].toInt()
            occurrences[index] = (occurrences[index] ?: 0) + 1
        }

        val missing = blocks.indices.filter { (occurrences[it] ?: 0) == 0 }
        val duplicated = occurrences.filterKeys { it in blocks.indices }.filterValues { it > 1 }.keys.sorted()
        val unknown = occurrences.keys.filterNot { it in blocks.indices }.sorted()

        if (missing.isNotEmpty() || duplicated.isNotEmpty() || unknown.isNotEmpty()) {
            throw PlaceholderMismatchException(missing, duplicated, unknown)
        }

        return PLACEHOLDER_REGEX.replace(translated) { match ->
            blocks[match.groupValues[1].toInt()]
        }
    }

    private fun findFrontMatterRange(markdown: String): IntRange? {
        if (!markdown.startsWith("---")) return null

        val firstLineEnd = markdown.indexOf('\n').let { if (it == -1) markdown.length else it }
        if (firstLineEnd == markdown.length) return null
        if (markdown.substring(0, firstLineEnd).trimEnd('\r') != "---") return null

        var searchStart = firstLineEnd + 1
        while (searchStart <= markdown.length) {
            val lineEnd = markdown.indexOf('\n', searchStart).let { if (it == -1) markdown.length else it }
            val line = markdown.substring(searchStart, lineEnd).trimEnd('\r')
            if (line == "---") {
                return 0 until lineEnd
            }
            if (lineEnd == markdown.length) break
            searchStart = lineEnd + 1
        }
        return null
    }

    private fun findFencedCodeRanges(markdown: String): List<IntRange> {
        val parser = MarkdownParser(GFMFlavourDescriptor(), assertionsEnabled = false, cancellationToken = CancellationToken.NonCancellable)
        val tree = parser.buildMarkdownTreeFromString(markdown as CharSequence)
        val ranges = mutableListOf<IntRange>()

        fun walk(node: ASTNode) {
            if (node.type == MarkdownElementTypes.CODE_FENCE) {
                ranges += node.startOffset until node.endOffset
            }
            node.children.forEach(::walk)
        }
        walk(tree)

        return ranges
    }
}
