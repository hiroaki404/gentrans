package io.github.hiroaki404.gentrans.plugin.preview

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.ui.components.JBHtmlPane
import com.intellij.ui.components.JBHtmlPaneConfiguration
import com.intellij.ui.components.JBHtmlPaneStyleConfiguration
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.ExtendableHTMLViewFactory
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.StyleSheetUtil
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import org.jetbrains.annotations.TestOnly
import java.awt.CardLayout
import java.util.concurrent.atomic.AtomicLong
import javax.swing.JComponent
import javax.swing.JPanel

internal class JBHtmlPaneTranslationPreview : TranslationPreview {
    private val cards = CardLayout()
    private val wrapper = JPanel(cards)
    private val messageArea = JBTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        border = JBUI.Borders.empty(8)
    }
    private val htmlPane = createConfiguredJBHtmlPane()
    private val generation = AtomicLong()
    private var shownCard = MESSAGE

    override val component: JComponent = wrapper

    init {
        wrapper.add(JBScrollPane(messageArea), MESSAGE)
        wrapper.add(JBScrollPane(htmlPane), PREVIEW)
    }

    override fun render(markdown: String) {
        val requested = generation.incrementAndGet()
        ApplicationManager.getApplication().executeOnPooledThread {
            val flavour = GFMFlavourDescriptor()
            val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
            val html = "<html>${HtmlGenerator(markdown, tree, flavour).generateHtml()}</html>"
            ApplicationManager.getApplication().invokeLater {
                if (generation.get() == requested) {
                    htmlPane.text = html
                    cards.show(wrapper, PREVIEW)
                    shownCard = PREVIEW
                }
            }
        }
    }

    override fun showMessage(message: String) {
        generation.incrementAndGet()
        messageArea.text = message
        cards.show(wrapper, MESSAGE)
        shownCard = MESSAGE
    }

    override fun dispose() = htmlPane.dispose()

    @TestOnly
    internal fun isShowingPreview(): Boolean = shownCard == PREVIEW

    @TestOnly
    internal fun renderedHtml(): String = htmlPane.text

    @TestOnly
    internal fun shownMessage(): String = messageArea.text

    private companion object {
        const val MESSAGE = "message"
        const val PREVIEW = "preview"
    }
}

internal fun createConfiguredJBHtmlPane(): JBHtmlPane = JBHtmlPane(
    JBHtmlPaneStyleConfiguration {
        colorSchemeProvider = { EditorColorsManager.getInstance().globalScheme }
        enableCodeBlocksBackground = true
        enableInlineCodeBackground = true
    },
    JBHtmlPaneConfiguration.builder()
        .extensions(ExtendableHTMLViewFactory.Extensions.WORD_WRAP, ExtendableHTMLViewFactory.Extensions.FIT_TO_WIDTH_IMAGES)
        .customStyleSheetProvider { StyleSheetUtil.loadStyleSheet(translationPreviewStyleSheet()) }
        .build(),
)

internal fun translationPreviewStyleSheet(): String = """
    body { margin-left: 8px; }
    blockquote { padding-left: 8px; }
    table { border-collapse: collapse; }
    th, td { border: 1px solid; }
""".trimIndent()
