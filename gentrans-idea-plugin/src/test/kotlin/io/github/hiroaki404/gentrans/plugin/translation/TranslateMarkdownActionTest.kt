package io.github.hiroaki404.gentrans.plugin.translation

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileTypes.FileType
import com.intellij.testFramework.MapDataContext
import com.intellij.testFramework.TestActionEvent
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import javax.swing.Icon

private class StubFileType(private val fileTypeName: String) : FileType {
    override fun getName(): String = fileTypeName

    override fun getDescription(): String = fileTypeName

    override fun getDefaultExtension(): String = ""

    override fun getIcon(): Icon? = null

    override fun isBinary(): Boolean = false
}

class TranslateMarkdownActionTest : BasePlatformTestCase() {
    fun testIsMarkdownFileTypeMatchesOnlyTheMarkdownFileType() {
        assertTrue(isMarkdownFileType(StubFileType(MARKDOWN_FILE_TYPE_NAME)))
        assertFalse(isMarkdownFileType(StubFileType("PLAIN_TEXT")))
        assertFalse(isMarkdownFileType(null))
    }

    fun testDisabledWithNoProject() {
        val action = TranslateMarkdownAction()
        val dataContext = MapDataContext().apply {
            put(CommonDataKeys.VIRTUAL_FILE, myFixture.configureByText("test.txt", "Hello").virtualFile)
        }

        val event = TestActionEvent.createTestEvent(action, dataContext)
        action.update(event)

        assertFalse(event.presentation.isEnabledAndVisible)
    }
}
