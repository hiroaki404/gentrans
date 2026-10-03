package io.github.hiroaki404.gentrans.plugin.translation

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Alarm
import io.github.hiroaki404.gentrans.core.api.TranslationEvent
import io.github.hiroaki404.gentrans.plugin.GentransBundle
import io.github.hiroaki404.gentrans.plugin.preview.JBHtmlPaneTranslationPreview
import io.github.hiroaki404.gentrans.plugin.preview.TranslationPreview
import java.awt.datatransfer.StringSelection
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executor
import javax.swing.JComponent

@Service(Service.Level.PROJECT)
internal class GentransToolWindowPanel @JvmOverloads constructor(
    private val project: Project,
    private val store: TranslationCacheStore = FileAttributeTranslationCacheStore.getInstance(),
    private val preview: TranslationPreview = JBHtmlPaneTranslationPreview(),
    private val translationService: TranslationService = TranslationService.getInstance(project),
    private val cacheExecutor: Executor = Executor { ApplicationManager.getApplication().executeOnPooledThread(it) },
    staleCheckScheduler: ((Runnable) -> Unit)? = null,
) : Disposable {
    private class InFlight(
        val file: VirtualFile?,
        val fileHash: String,
        val sourceHash: String,
        val isSelection: Boolean,
        var targetLanguage: String? = null,
        var partialText: String = "",
        var origin: TranslationOrigin? = null,
    )

    private val panel = SimpleToolWindowPanel(true, true)
    private var inFlight: InFlight? = null
    private var displayedFile: VirtualFile? = null
    private var displayedTranslation: CachedTranslation? = null
    private var displayedStale = false
    private var lastCompleted: Pair<VirtualFile?, CachedTranslation>? = null
    private var displayRequest = 0L
    private val scheduleStaleCheck: (Runnable) -> Unit = staleCheckScheduler
        ?: Alarm(Alarm.ThreadToUse.SWING_THREAD, this).let { alarm ->
            { runnable: Runnable ->
                alarm.cancelAllRequests()
                alarm.addRequest(runnable, STALE_CHECK_DELAY_MS)
            }
        }

    val component: JComponent = panel
    internal val displayedTranslationText: String? get() = displayedTranslation?.translatedText

    init {
        Disposer.register(this, preview)
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                val file = FileDocumentManager.getInstance().getFile(event.document)
                if (file == displayedFile && displayedTranslation != null && inFlight?.file != file) {
                    scheduleStaleCheck(Runnable { recheckStale() })
                }
            }
        }, this)
        panel.toolbar = ActionManager.getInstance().createActionToolbar(
            "GenTrans",
            DefaultActionGroup(CopyAction(), SaveAction(), CancelAction(), RerunAction()),
            true,
        ).component
        panel.setContent(preview.component)
        project.messageBus.connect(this).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun selectionChanged(event: FileEditorManagerEvent) {
                val file = event.newFile ?: return
                if (isMarkdownFileType(FileTypeRegistry.getInstance().getFileTypeByFile(file))) selectFile(file)
            }
        })
        FileEditorManager.getInstance(project).currentFile?.let { file ->
            if (isMarkdownFileType(FileTypeRegistry.getInstance().getFileTypeByFile(file))) selectFile(file)
        }
    }

    fun startTranslation(sourceFile: VirtualFile?, text: String, wholeFileText: String, isSelection: Boolean) {
        val translation = InFlight(sourceFile, sha256(wholeFileText), sha256(text), isSelection)
        inFlight = translation
        displayedFile = sourceFile
        displayedTranslation = null
        displayedStale = false
        lastCompleted = null
        displayRequest++
        preview.showMessage(GentransBundle.message("gentrans.preview.translating"))
        translationService.translate(
            text,
            onEvent = { event -> handleEvent(translation, event) },
            onFailure = { failure -> handleFailure(translation, failure) },
            onCompletedOrigin = { origin -> translation.origin = origin },
        )
    }

    internal fun selectFile(file: VirtualFile) {
        if (displayedFile != file) lastCompleted = null
        displayedFile = file
        displayedTranslation = null
        displayedStale = false
        displayRequest++
        val translation = inFlight
        if (translation?.file == file) {
            showInFlight(translation)
        } else {
            refreshDisplayed()
        }
    }

    fun refreshDisplayed() {
        val file = displayedFile ?: return
        val translation = inFlight
        if (translation?.file == file) showInFlight(translation)
        val request = ++displayRequest
        displayedTranslation = lastCompleted?.takeIf { it.first == file }?.second
        displayedStale = false
        cacheExecutor.execute {
            val entry = try {
                store.get(file)
            } catch (error: Exception) {
                LOG.warn("Failed to read translation cache for ${file.path}", error)
                null
            }
            val fileText = try {
                String(file.contentsToByteArray(), StandardCharsets.UTF_8)
            } catch (error: Exception) {
                LOG.warn("Failed to read source file for ${file.path}", error)
                ""
            }
            val display = {
                if (!project.isDisposed && displayedFile == file && displayRequest == request) {
                    val currentTranslation = inFlight
                    if (currentTranslation?.file == file) {
                        showInFlight(currentTranslation)
                    } else {
                        val shownEntry = entry ?: lastCompleted?.takeIf { it.first == file }?.second
                        displayedTranslation = shownEntry
                        if (shownEntry == null) {
                            displayedStale = false
                            preview.showMessage(GentransBundle.message("gentrans.preview.untranslated"))
                        } else {
                            val currentText = FileDocumentManager.getInstance().getDocument(file)?.text ?: fileText
                            val isStale = shownEntry.isStale(currentText)
                            displayedStale = isStale
                            preview.render(shownEntry.translatedText, translationNotice(shownEntry.isSelection, isStale))
                        }
                    }
                }
            }
            if (ApplicationManager.getApplication().isDispatchThread) display()
            else ApplicationManager.getApplication().invokeLater { display() }
        }
    }

    private fun recheckStale() {
        val file = displayedFile ?: return
        val entry = displayedTranslation ?: return
        if (inFlight?.file == file) return
        val request = displayRequest
        val currentText = FileDocumentManager.getInstance().getDocument(file)?.immutableCharSequence?.toString() ?: return
        cacheExecutor.execute {
            val hash = sha256(currentText)
            val display = {
                if (!project.isDisposed && displayedFile == file && displayRequest == request && displayedTranslation === entry) {
                    val isStale = entry.fileHash != hash
                    if (displayedStale != isStale) {
                        displayedStale = isStale
                        preview.render(entry.translatedText, translationNotice(entry.isSelection, isStale))
                    }
                }
            }
            if (ApplicationManager.getApplication().isDispatchThread) display()
            else ApplicationManager.getApplication().invokeLater { display() }
        }
    }

    fun onCacheCleared() {
        lastCompleted = null
        displayedTranslation = null
        displayedStale = false
        refreshDisplayed()
    }

    internal fun cancelTranslation() {
        val translation = inFlight ?: return
        inFlight = null
        translationService.cancel()
        if (displayedFile == translation.file) refreshDisplayed()
    }

    private fun showInFlight(translation: InFlight) {
        if (translation.partialText.isEmpty()) preview.showMessage(GentransBundle.message("gentrans.preview.translating"))
        else preview.render(translation.partialText, translationNotice(translation.isSelection))
    }

    private fun translationNotice(isSelection: Boolean, isStale: Boolean = false): String? =
        listOfNotNull(
            GentransBundle.message("gentrans.preview.staleNotice").takeIf { isStale },
            GentransBundle.message("gentrans.preview.selectionNotice").takeIf { isSelection },
        ).takeIf { it.isNotEmpty() }?.joinToString("\n")

    private fun handleEvent(translation: InFlight, event: TranslationEvent) {
        if (inFlight !== translation) return
        when (event) {
            is TranslationEvent.SourceLanguageDetected -> Unit
            is TranslationEvent.TargetLanguageDecided -> translation.targetLanguage = event.language
            is TranslationEvent.ChunkTranslated -> {
                translation.partialText = event.translatedTextSoFar
                if (displayedFile == translation.file) preview.render(translation.partialText, translationNotice(translation.isSelection))
            }
            is TranslationEvent.Completed -> {
                inFlight = null
                val origin = translation.origin
                val generation = try {
                    store.currentGeneration()
                } catch (error: Exception) {
                    LOG.warn("Failed to prepare translation cache entry", error)
                    null
                }
                val entry = CachedTranslation(
                    fileHash = translation.fileHash,
                    sourceHash = translation.sourceHash,
                    isSelection = translation.isSelection,
                    targetLanguage = translation.targetLanguage.orEmpty(),
                    provider = origin?.provider.orEmpty(),
                    model = origin?.model.orEmpty(),
                    translatedText = event.text,
                    generation = generation ?: 0L,
                )
                if (generation != null && translation.file != null) {
                    try {
                        store.put(translation.file, entry)
                    } catch (error: Exception) {
                        LOG.warn("Failed to store translation cache for ${translation.file.path}", error)
                    }
                }
                if (displayedFile == translation.file) {
                    displayRequest++
                    displayedTranslation = entry
                    lastCompleted = translation.file to entry
                    val currentText = translation.file?.let { FileDocumentManager.getInstance().getDocument(it)?.text }
                    val isStale = currentText != null && translation.fileHash != sha256(currentText)
                    displayedStale = isStale
                    preview.render(event.text, translationNotice(translation.isSelection, isStale))
                }
            }
            is TranslationEvent.Summarized -> Unit
        }
    }

    private fun handleFailure(translation: InFlight, failure: TranslationFailure) {
        if (inFlight !== translation) return
        inFlight = null
        if (displayedFile != translation.file) return
        val message = when (failure) {
            is TranslationFailure.ApiKeyMissing -> GentransBundle.message("gentrans.failure.apiKeyMissing", failure.provider.key)
            is TranslationFailure.Generic -> GentransBundle.message("gentrans.failure.generic", failure.message)
        }
        preview.showMessage(message)
    }

    private inner class CopyAction : AnAction(GentransBundle.message("gentrans.toolbar.copy")) {
        override fun actionPerformed(event: AnActionEvent) {
            displayedTranslationText?.let { CopyPasteManager.getInstance().setContents(StringSelection(it)) }
        }
    }

    private inner class SaveAction : AnAction(GentransBundle.message("gentrans.toolbar.saveAs")) {
        override fun actionPerformed(event: AnActionEvent) {
            val source = displayedFile ?: return
            val translation = displayedTranslation ?: return
            val parent = source.parent ?: return
            val name = "${source.nameWithoutExtension}.${translation.targetLanguage.lowercase().replace(Regex("\\s+"), "-")}.md"
            val existing = parent.findChild(name)
            if (existing != null && Messages.showYesNoDialog(project, GentransBundle.message("gentrans.save.overwrite.message", name), GentransBundle.message("gentrans.save.overwrite.title"), null) != Messages.YES) return
            try {
                runWriteAction {
                    val file = existing ?: parent.createChildData(this, name)
                    file.setBinaryContent(translation.translatedText.toByteArray(StandardCharsets.UTF_8))
                }
            } catch (error: Exception) {
                preview.showMessage(GentransBundle.message("gentrans.save.error", error.message ?: error.javaClass.simpleName))
            }
        }
    }

    private inner class CancelAction : AnAction(GentransBundle.message("gentrans.toolbar.cancel")) {
        override fun actionPerformed(event: AnActionEvent) = cancelTranslation()
    }

    private inner class RerunAction : AnAction(GentransBundle.message("gentrans.toolbar.rerun")) {
        override fun actionPerformed(event: AnActionEvent) {
            val file = displayedFile ?: return
            val text = FileDocumentManager.getInstance().getDocument(file)?.text
                ?: String(file.contentsToByteArray(), StandardCharsets.UTF_8)
            startTranslation(file, text, text, false)
        }
    }

    override fun dispose() = Unit

    companion object {
        const val STALE_CHECK_DELAY_MS = 1000
        private val LOG = Logger.getInstance(GentransToolWindowPanel::class.java)

        fun getInstance(project: Project): GentransToolWindowPanel = project.service()
    }
}
