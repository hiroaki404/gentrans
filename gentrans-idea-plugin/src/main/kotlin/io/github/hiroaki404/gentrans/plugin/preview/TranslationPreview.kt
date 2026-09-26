package io.github.hiroaki404.gentrans.plugin.preview

import com.intellij.openapi.Disposable
import javax.swing.JComponent

internal interface TranslationPreview : Disposable {
    val component: JComponent
    fun render(markdown: String)
    fun showMessage(message: String)
}
