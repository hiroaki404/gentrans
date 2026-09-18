package io.github.hiroaki404.gentrans.core.api

/**
 * Events reporting translation progress to the frontend (CLI / IDE plugin).
 *
 * A boundary that hides Koog types (`AIAgent`, `PromptExecutor`, strategy nodes) while still
 * exposing progress. Failures are thrown as exceptions from the `Flow`, not delivered as events.
 */
public sealed interface TranslationEvent {
    /** Source language detection finished. [totalChunks] is the number of chunks after splitting. */
    public data class SourceLanguageDetected(val language: String, val totalChunks: Int) : TranslationEvent

    /** The target language for translation was decided. */
    public data class TargetLanguageDecided(val language: String) : TranslationEvent

    /** One chunk of summarization (`--summary`) finished. [remainingChunks] is the number remaining. */
    public data class Summarized(val remainingChunks: Int) : TranslationEvent

    /**
     * One chunk of translation finished.
     *
     * [chunk] is that chunk's translated text, [translatedTextSoFar] is the concatenation of all
     * translated text so far.
     */
    public data class ChunkTranslated(
        val chunk: String,
        val translatedTextSoFar: String,
        val translatedChunks: Int,
        val totalChunks: Int,
    ) : TranslationEvent

    /** Translation completed. [text] is the final result. */
    public data class Completed(val text: String) : TranslationEvent
}
