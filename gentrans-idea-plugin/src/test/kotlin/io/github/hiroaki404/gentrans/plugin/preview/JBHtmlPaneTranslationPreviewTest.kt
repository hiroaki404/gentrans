package io.github.hiroaki404.gentrans.plugin.preview

import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JBHtmlPaneTranslationPreviewTest : BasePlatformTestCase() {
    fun testTranslationPreviewStyleSheetDefinesThemeFriendlyLayoutRules() {
        val styleSheet = translationPreviewStyleSheet()

        assertTrue(styleSheet.contains("body { margin-left: 8px; }"))
        assertTrue(styleSheet.contains("blockquote { padding-left: 8px; }"))
        assertTrue(styleSheet.contains("table { border-collapse: collapse; }"))
        assertTrue(styleSheet.contains("th, td { border: 1px solid; }"))
    }

    fun testRenderConvertsMarkdownToHtmlAndSwitchesToThePreviewCard() {
        val preview = JBHtmlPaneTranslationPreview()
        try {
            preview.showMessage("placeholder")
            preview.render("# Hello")

            PlatformTestUtil.waitWhileBusy { !preview.isShowingPreview() }

            assertTrue(preview.isShowingPreview())
            assertTrue(preview.renderedHtml().contains("Hello"))
        } finally {
            Disposer.dispose(preview)
        }
    }

    fun testRenderWrapsFencedCodeBlocksForThemeAwareBackground() {
        val preview = JBHtmlPaneTranslationPreview()
        try {
            preview.render("```kotlin\nfun greet() {}\n```")

            PlatformTestUtil.waitWhileBusy { !preview.isShowingPreview() }

            assertTrue(preview.renderedHtml().contains("class=\"code-block\""))
        } finally {
            Disposer.dispose(preview)
        }
    }

    fun testRenderEscapesNoticeAboveMarkdown() {
        val preview = JBHtmlPaneTranslationPreview()
        try {
            preview.render("# Hello", "Old <source> & translation")
            PlatformTestUtil.waitWhileBusy { !preview.isShowingPreview() }

            val html = preview.renderedHtml()
            assertTrue(html.contains("Old &lt;source&gt; &amp; translation"))
            assertTrue(html.indexOf("Old &lt;source&gt;") < html.indexOf("Hello"))
        } finally {
            Disposer.dispose(preview)
        }
    }

    fun testShowMessageSwitchesBackToTheMessageCard() {
        val preview = JBHtmlPaneTranslationPreview()
        try {
            preview.render("# Hello")
            PlatformTestUtil.waitWhileBusy { !preview.isShowingPreview() }

            preview.showMessage("Translating…")

            assertFalse(preview.isShowingPreview())
            assertEquals("Translating…", preview.shownMessage())
        } finally {
            Disposer.dispose(preview)
        }
    }
}
