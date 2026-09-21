package io.github.hiroaki404.gentrans.plugin.preview

import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JBHtmlPaneTranslationPreviewTest : BasePlatformTestCase() {
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
