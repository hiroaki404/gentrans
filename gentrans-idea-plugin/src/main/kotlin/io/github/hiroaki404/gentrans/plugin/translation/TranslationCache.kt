package io.github.hiroaki404.gentrans.plugin.translation

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileWithId
import com.intellij.openapi.vfs.newvfs.FileAttribute
import com.intellij.util.io.IOUtil
import io.github.hiroaki404.gentrans.plugin.settings.GentransSettings
import java.security.MessageDigest

internal data class CachedTranslation(
    val fileHash: String,
    val sourceHash: String,
    val isSelection: Boolean,
    val targetLanguage: String,
    val provider: String,
    val model: String,
    val translatedText: String,
    val generation: Long,
)

internal interface TranslationCacheStore {
    fun get(file: VirtualFile): CachedTranslation?
    fun put(file: VirtualFile, entry: CachedTranslation)
    fun clearAll()
    fun currentGeneration(): Long
}

internal fun sha256(text: String): String =
    MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

internal fun CachedTranslation.isStale(currentFileText: String): Boolean = fileHash != sha256(currentFileText)

@Service(Service.Level.APP)
internal class FileAttributeTranslationCacheStore @JvmOverloads constructor(
    private val settingsProvider: () -> GentransSettings = GentransSettings::getInstance,
) : TranslationCacheStore {
    override fun get(file: VirtualFile): CachedTranslation? {
        if (file !is VirtualFileWithId) return null
        return try {
            val entry = ATTRIBUTE.readFileAttribute(file)?.use { input ->
                CachedTranslation(
                    fileHash = IOUtil.readString(input),
                    sourceHash = IOUtil.readString(input),
                    isSelection = input.readBoolean(),
                    targetLanguage = IOUtil.readString(input),
                    provider = IOUtil.readString(input),
                    model = IOUtil.readString(input),
                    translatedText = IOUtil.readString(input),
                    generation = input.readLong(),
                )
            }
            entry?.takeIf { it.generation == currentGeneration() }
        } catch (error: Exception) {
            LOG.warn("Failed to read translation cache for ${file.path}", error)
            null
        }
    }

    override fun put(file: VirtualFile, entry: CachedTranslation) {
        if (file !is VirtualFileWithId) return
        ATTRIBUTE.writeFileAttribute(file).use { output ->
            IOUtil.writeString(entry.fileHash, output)
            IOUtil.writeString(entry.sourceHash, output)
            output.writeBoolean(entry.isSelection)
            IOUtil.writeString(entry.targetLanguage, output)
            IOUtil.writeString(entry.provider, output)
            IOUtil.writeString(entry.model, output)
            IOUtil.writeString(entry.translatedText, output)
            output.writeLong(entry.generation)
        }
    }

    override fun clearAll() {
        settingsProvider().state.cacheGeneration++
    }

    override fun currentGeneration(): Long = settingsProvider().state.cacheGeneration

    companion object {
        private val LOG = Logger.getInstance(FileAttributeTranslationCacheStore::class.java)
        private val ATTRIBUTE = FileAttribute("gentrans.translation", 1, false)

        fun getInstance(): FileAttributeTranslationCacheStore =
            ApplicationManager.getApplication().getService(FileAttributeTranslationCacheStore::class.java)
    }
}
