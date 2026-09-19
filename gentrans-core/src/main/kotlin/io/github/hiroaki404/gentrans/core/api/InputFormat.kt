package io.github.hiroaki404.gentrans.core.api

/**
 * The format of [TranslationRequest.text], controlling how it is split into chunks and how the
 * translation prompt is built.
 */
public enum class InputFormat {
    /** Plain, unstructured text. Split by line, with no format-specific prompt rules. */
    PLAIN_TEXT,

    /**
     * Markdown text. Front matter and fenced code blocks are protected from translation and
     * restored verbatim afterward; the translation prompt is additionally instructed to preserve
     * Markdown syntax.
     */
    MARKDOWN,
}
