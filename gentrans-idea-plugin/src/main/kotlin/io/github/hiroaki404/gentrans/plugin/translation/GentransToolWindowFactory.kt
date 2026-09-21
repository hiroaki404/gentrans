package io.github.hiroaki404.gentrans.plugin.translation

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

internal class GentransToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = GentransToolWindowPanel.getInstance(project)
        toolWindow.contentManager.addContent(ContentFactory.getInstance().createContent(panel.component, "", false))
    }
}
