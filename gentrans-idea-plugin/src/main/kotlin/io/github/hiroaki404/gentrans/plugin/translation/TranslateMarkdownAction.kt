package io.github.hiroaki404.gentrans.plugin.translation

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import io.github.hiroaki404.gentrans.core.api.InputFormat
import io.github.hiroaki404.gentrans.core.api.estimateChunkCount
import io.github.hiroaki404.gentrans.plugin.GentransBundle
import java.nio.charset.StandardCharsets

internal const val MARKDOWN_FILE_TYPE_NAME = "Markdown"

internal fun isMarkdownFileType(fileType: FileType?): Boolean = fileType?.name == MARKDOWN_FILE_TYPE_NAME

internal fun translationText(
    selectedText: String?,
    editorText: String?,
    fileText: String?,
): String? = selectedText?.takeIf { it.isNotEmpty() } ?: editorText ?: fileText

internal fun isTranslationSelection(selectedText: String?): Boolean = !selectedText.isNullOrEmpty()

internal class TranslateMarkdownAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
        event.presentation.isEnabledAndVisible = event.project != null &&
            isMarkdownFileType(file?.let { FileTypeRegistry.getInstance().getFileTypeByFile(it) })
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val editor = event.getData(CommonDataKeys.EDITOR)
        val selectedText = editor?.selectionModel?.selectedText
        val editorText = editor?.document?.text
        val fileText = file?.let {
            FileDocumentManager.getInstance().getDocument(it)?.text
                ?: runCatching { String(it.contentsToByteArray(), StandardCharsets.UTF_8) }.getOrNull()
        }
        val text = translationText(
            selectedText = selectedText,
            editorText = editorText,
            fileText = fileText,
        ) ?: return
        val wholeFileText = editorText ?: fileText ?: text
        val isSelection = isTranslationSelection(selectedText)
        ApplicationManager.getApplication().executeOnPooledThread {
            val chunks = estimateChunkCount(text, InputFormat.MARKDOWN)
            ApplicationManager.getApplication().invokeLater {
                if (chunks >= CONFIRMATION_CHUNK_THRESHOLD) {
                    val confirmed = Messages.showYesNoDialog(
                        project,
                        GentransBundle.message("gentrans.confirm.message", chunks, text.length),
                        GentransBundle.message("gentrans.confirm.title"),
                        null,
                    ) == Messages.YES
                    if (!confirmed) return@invokeLater
                }
                ToolWindowManager.getInstance(project).getToolWindow("GenTrans")?.show {
                    GentransToolWindowPanel.getInstance(project).startTranslation(file, text, wholeFileText, isSelection)
                }
            }
        }
    }

    private companion object {
        const val CONFIRMATION_CHUNK_THRESHOLD = 5
    }
}
