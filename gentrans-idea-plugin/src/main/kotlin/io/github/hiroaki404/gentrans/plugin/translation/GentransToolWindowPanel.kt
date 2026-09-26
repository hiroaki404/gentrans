package io.github.hiroaki404.gentrans.plugin.translation

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import io.github.hiroaki404.gentrans.core.api.TranslationEvent
import io.github.hiroaki404.gentrans.plugin.GentransBundle
import io.github.hiroaki404.gentrans.plugin.preview.JBHtmlPaneTranslationPreview
import io.github.hiroaki404.gentrans.plugin.preview.TranslationPreview
import java.awt.datatransfer.StringSelection
import java.nio.charset.StandardCharsets
import javax.swing.JComponent

@Service(Service.Level.PROJECT)
internal class GentransToolWindowPanel(
    private val project: Project,
) : Disposable {
    private val preview: TranslationPreview = JBHtmlPaneTranslationPreview()
    private val panel = SimpleToolWindowPanel(true, true)
    private var sourceFile: VirtualFile? = null
    private var targetLanguage: String? = null
    private var translatedText = ""
    private var lastSourceText: String? = null

    val component: JComponent = panel

    init {
        Disposer.register(this, preview)
        panel.toolbar = ActionManager.getInstance().createActionToolbar(
            "GenTrans",
            DefaultActionGroup(CopyAction(), SaveAction(), CancelAction(), RerunAction()),
            true,
        ).component
        panel.setContent(preview.component)
    }

    fun startTranslation(sourceFile: VirtualFile?, text: String) {
        this.sourceFile = sourceFile
        targetLanguage = null
        translatedText = ""
        lastSourceText = text
        preview.showMessage(GentransBundle.message("gentrans.preview.translating"))
        TranslationService.getInstance(project).translate(text, ::handleEvent, ::handleFailure)
    }

    private fun handleEvent(event: TranslationEvent) {
        when (event) {
            is TranslationEvent.SourceLanguageDetected -> if (translatedText.isEmpty()) {
                preview.showMessage(GentransBundle.message("gentrans.preview.translating"))
            }
            is TranslationEvent.TargetLanguageDecided -> targetLanguage = event.language
            is TranslationEvent.ChunkTranslated -> {
                translatedText = event.translatedTextSoFar
                preview.render(translatedText)
            }
            is TranslationEvent.Completed -> {
                translatedText = event.text
                preview.render(translatedText)
            }
            is TranslationEvent.Summarized -> Unit
        }
    }

    private fun handleFailure(failure: TranslationFailure) {
        val message = when (failure) {
            is TranslationFailure.ApiKeyMissing -> GentransBundle.message("gentrans.failure.apiKeyMissing", failure.provider.key)
            is TranslationFailure.Generic -> GentransBundle.message("gentrans.failure.generic", failure.message)
        }
        preview.showMessage(message)
    }

    private inner class CopyAction : AnAction(GentransBundle.message("gentrans.toolbar.copy")) {
        override fun actionPerformed(event: AnActionEvent) {
            CopyPasteManager.getInstance().setContents(StringSelection(translatedText))
        }
    }

    private inner class SaveAction : AnAction(GentransBundle.message("gentrans.toolbar.saveAs")) {
        override fun actionPerformed(event: AnActionEvent) {
            val source = sourceFile ?: return
            val language = targetLanguage ?: return
            val parent = source.parent ?: return
            val name = "${source.nameWithoutExtension}.${language.lowercase().replace(Regex("\\s+"), "-")}.md"
            val existing = parent.findChild(name)
            if (existing != null && Messages.showYesNoDialog(project, GentransBundle.message("gentrans.save.overwrite.message", name), GentransBundle.message("gentrans.save.overwrite.title"), null) != Messages.YES) return
            try {
                runWriteAction {
                    val file = existing ?: parent.createChildData(this, name)
                    file.setBinaryContent(translatedText.toByteArray(StandardCharsets.UTF_8))
                }
            } catch (error: Exception) {
                preview.showMessage(GentransBundle.message("gentrans.save.error", error.message ?: error.javaClass.simpleName))
            }
        }
    }

    private inner class CancelAction : AnAction(GentransBundle.message("gentrans.toolbar.cancel")) {
        override fun actionPerformed(event: AnActionEvent) = TranslationService.getInstance(project).cancel()
    }

    private inner class RerunAction : AnAction(GentransBundle.message("gentrans.toolbar.rerun")) {
        override fun actionPerformed(event: AnActionEvent) {
            lastSourceText?.let { startTranslation(sourceFile, it) }
        }
    }

    override fun dispose() = Unit

    companion object {
        fun getInstance(project: Project): GentransToolWindowPanel = project.service()
    }
}
