package io.github.hiroaki404.gentrans.plugin.translation

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.hiroaki404.gentrans.core.api.InternalGentransApi
import io.github.hiroaki404.gentrans.core.api.TranslatorConfig
import io.github.hiroaki404.gentrans.core.api.createTranslator
import io.github.hiroaki404.gentrans.plugin.GentransBundle
import io.github.hiroaki404.gentrans.plugin.preview.TranslationPreview
import io.github.hiroaki404.gentrans.plugin.settings.GentransSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.concurrent.Executor
import javax.swing.JComponent
import javax.swing.JPanel

private class PanelCacheStore : TranslationCacheStore {
    val entries = mutableMapOf<VirtualFile, CachedTranslation>()
    var generation = 7L
    var reads = 0
    var failPut = false
    var persist = true
    @Volatile var lastReadOnEdt: Boolean? = null

    override fun get(file: VirtualFile): CachedTranslation? {
        reads++
        lastReadOnEdt = ApplicationManager.getApplication().isDispatchThread
        return entries[file]?.takeIf { it.generation == generation }
    }

    override fun put(file: VirtualFile, entry: CachedTranslation) {
        if (failPut) throw IllegalStateException("cache unavailable")
        if (persist) entries[file] = entry
    }

    override fun clearAll() {
        generation++
        entries.clear()
    }

    override fun currentGeneration(): Long = generation
}

private class PanelPreview : TranslationPreview {
    override val component: JComponent = JPanel()
    var markdown: String? = null
    var notice: String? = null
    var message: String? = null
    val renderedNotices = mutableListOf<String?>()

    override fun render(markdown: String, notice: String?) {
        this.markdown = markdown
        this.notice = notice
        renderedNotices += notice
        message = null
    }

    override fun showMessage(message: String) {
        this.message = message
        markdown = null
        notice = null
    }

    override fun dispose() = Unit
}

@OptIn(InternalGentransApi::class)
class GentransToolWindowPanelTest : BasePlatformTestCase() {
    private lateinit var scope: CoroutineScope
    private lateinit var settings: GentransSettings
    private lateinit var originalState: GentransSettings.State

    override fun setUp() {
        super.setUp()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        settings = GentransSettings.getInstance()
        originalState = settings.state
        settings.loadState(GentransSettings.State(provider = "openai", model = "gpt-4o-mini"))
    }

    override fun tearDown() {
        try {
            scope.cancel()
            settings.loadState(originalState)
        } finally {
            super.tearDown()
        }
    }

    fun testCompletedSelectionIsStoredWithTranslationMetadata() {
        val file = file("selection.md", "whole file")
        val store = PanelCacheStore()
        val preview = PanelPreview()
        val panel = panel(store, preview, service())

        panel.startTranslation(file, "selected", "whole file", true)
        PlatformTestUtil.waitWhileBusy { store.entries[file] == null }

        assertEquals(
            CachedTranslation(
                fileHash = sha256("whole file"),
                sourceHash = sha256("selected"),
                isSelection = true,
                targetLanguage = "English",
                provider = "openai",
                model = "gpt-4o-mini",
                translatedText = "Hello World!",
                generation = 7,
            ),
            store.entries[file],
        )
        assertEquals("Hello World!", preview.markdown)
        PlatformTestUtil.waitWhileBusy { preview.renderedNotices.size < 2 }
        assertEquals(
            listOf(
                GentransBundle.message("gentrans.preview.selectionNotice"),
                GentransBundle.message("gentrans.preview.selectionNotice"),
            ),
            preview.renderedNotices,
        )
    }

    fun testWholeFileTranslationRendersWithoutNotice() {
        val file = file("whole.md", "whole file")
        val preview = PanelPreview()
        val panel = panel(PanelCacheStore(), preview, service())

        panel.startTranslation(file, "whole file", "whole file", false)
        PlatformTestUtil.waitWhileBusy { preview.renderedNotices.size < 2 }

        assertEquals(listOf(null, null), preview.renderedNotices)
    }

    fun testSelectionRendersCachedTranslationNoticesAndUntranslatedMessage() {
        val file = file("cached.md", "current")
        val missing = file("missing.md", "other")
        val store = PanelCacheStore()
        val preview = PanelPreview()
        val panel = panel(store, preview, service())
        store.entries[file] = entry("current", "Cached", isSelection = true)

        panel.selectFile(file)
        assertEquals("Cached", preview.markdown)
        assertEquals(GentransBundle.message("gentrans.preview.selectionNotice"), preview.notice)

        store.entries[file] = entry("old", "Stale", isSelection = true)
        panel.refreshDisplayed()
        assertEquals("Stale", preview.markdown)
        assertTrue(preview.notice!!.contains(GentransBundle.message("gentrans.preview.staleNotice")))
        assertTrue(preview.notice!!.contains(GentransBundle.message("gentrans.preview.selectionNotice")))

        panel.selectFile(missing)
        assertEquals(GentransBundle.message("gentrans.preview.untranslated"), preview.message)
    }

    fun testSwitchingFilesDoesNotCancelOrOverwriteOtherFilesPreview() {
        val first = file("first.md", "first")
        val second = file("second.md", "second")
        val store = PanelCacheStore()
        val preview = PanelPreview()
        val entered = CompletableDeferred<Unit>()
        val gate = CompletableDeferred<Unit>()
        val panel = panel(store, preview, service(ScriptedPromptExecutor(entered, gate, gateAtCallIndex = 1)))

        panel.startTranslation(first, "first", "first", false)
        PlatformTestUtil.waitWhileBusy { !entered.isCompleted }
        panel.selectFile(second)
        assertEquals(GentransBundle.message("gentrans.preview.untranslated"), preview.message)
        gate.complete(Unit)
        PlatformTestUtil.waitWhileBusy { store.entries[first] == null }

        assertEquals("Hello World!", store.entries[first]?.translatedText)
        assertEquals(GentransBundle.message("gentrans.preview.untranslated"), preview.message)
        assertNull(preview.markdown)
    }

    fun testCancelAndFailureDoNotStore() {
        val file = file("cancel.md", "source")
        val store = PanelCacheStore()
        val entered = CompletableDeferred<Unit>()
        val gate = CompletableDeferred<Unit>()
        val panel = panel(store, PanelPreview(), service(ScriptedPromptExecutor(entered, gate, gateAtCallIndex = 1)))

        panel.startTranslation(file, "source", "source", false)
        PlatformTestUtil.waitWhileBusy { !entered.isCompleted }
        panel.cancelTranslation()
        gate.complete(Unit)
        assertNull(store.entries[file])

        val failedPreview = PanelPreview()
        val failed = panel(store, failedPreview, TranslationService(project, scope, translatorFactory = {
            throw IllegalStateException("failed")
        }, apiKeyStore = FakeApiKeyStore()))
        failed.startTranslation(file, "source", "source", false)
        PlatformTestUtil.waitWhileBusy { failedPreview.message?.contains("failed") != true }
        assertNull(store.entries[file])
    }

    fun testCacheWriteFailureStillShowsCompletedTranslation() {
        val file = file("unwritable.md", "source")
        val store = PanelCacheStore().apply { failPut = true }
        val preview = PanelPreview()
        val panel = panel(store, preview, service())

        panel.startTranslation(file, "source", "source", false)
        PlatformTestUtil.waitWhileBusy { panel.displayedTranslationText != "Hello World!" }

        assertNull(store.entries[file])
        assertEquals("Hello World!", preview.markdown)
    }

    fun testCompletedTranslationRemainsAvailableWhenCacheDoesNotPersist() {
        val file = file("uncacheable.md", "source")
        val store = PanelCacheStore().apply { persist = false }
        val preview = PanelPreview()
        val panel = panel(store, preview, service())

        panel.startTranslation(file, "source", "source", false)
        PlatformTestUtil.waitWhileBusy { panel.displayedTranslationText != "Hello World!" }

        assertNull(store.entries[file])
        assertEquals("Hello World!", panel.displayedTranslationText)
        panel.refreshDisplayed()
        assertTrue(store.reads > 0)
        assertEquals("Hello World!", preview.markdown)
        assertEquals("Hello World!", panel.displayedTranslationText)
    }

    fun testCacheClearRemovesCompletedFallbackAndShowsUntranslated() {
        val file = file("cleared.md", "source")
        val store = PanelCacheStore().apply { persist = false }
        val preview = PanelPreview()
        val panel = panel(store, preview, service())

        panel.startTranslation(file, "source", "source", false)
        PlatformTestUtil.waitWhileBusy { panel.displayedTranslationText != "Hello World!" }
        panel.refreshDisplayed()
        assertEquals("Hello World!", preview.markdown)

        store.clearAll()
        panel.onCacheCleared()

        assertNull(panel.displayedTranslationText)
        assertEquals(GentransBundle.message("gentrans.preview.untranslated"), preview.message)
    }

    fun testCacheClearKeepsInFlightTranslationAndStoresItInNewGeneration() {
        val file = file("in-flight.md", "source")
        val store = PanelCacheStore()
        val preview = PanelPreview()
        val entered = CompletableDeferred<Unit>()
        val gate = CompletableDeferred<Unit>()
        val panel = panel(store, preview, service(ScriptedPromptExecutor(entered, gate, gateAtCallIndex = 1)))

        panel.startTranslation(file, "source", "source", false)
        PlatformTestUtil.waitWhileBusy { !entered.isCompleted }
        store.clearAll()
        panel.onCacheCleared()
        assertEquals(GentransBundle.message("gentrans.preview.translating"), preview.message)

        gate.complete(Unit)
        PlatformTestUtil.waitWhileBusy { store.entries[file] == null }

        assertEquals(8L, store.entries[file]?.generation)
        assertEquals("Hello World!", preview.markdown)
    }

    fun testCompletedTranslationWithoutSourceFileRemainsAvailableForCopy() {
        val preview = PanelPreview()
        val panel = panel(PanelCacheStore(), preview, service())

        panel.startTranslation(null, "source", "source", false)
        PlatformTestUtil.waitWhileBusy { panel.displayedTranslationText != "Hello World!" }

        assertEquals("Hello World!", panel.displayedTranslationText)
    }

    fun testRefreshDisplayedReadsCacheAgain() {
        val file = file("refresh.md", "source")
        val store = PanelCacheStore()
        val preview = PanelPreview()
        val panel = panel(store, preview, service())
        store.entries[file] = entry("source", "Before")

        panel.selectFile(file)
        val firstReads = store.reads
        store.entries[file] = entry("source", "After")
        panel.refreshDisplayed()

        assertTrue(store.reads > firstReads)
        assertEquals("After", preview.markdown)
    }

    fun testLateCacheResultCannotOverwriteAnotherFile() {
        val first = file("old.md", "old")
        val second = file("new.md", "new")
        val store = PanelCacheStore()
        val preview = PanelPreview()
        val pending = mutableListOf<Runnable>()
        val panel = GentransToolWindowPanel(project, store, preview, service(), Executor { pending += it })
        Disposer.register(testRootDisposable, panel)
        pending.clear()
        store.entries[first] = entry("old", "Old translation")
        store.entries[second] = entry("new", "New translation")

        panel.selectFile(first)
        panel.selectFile(second)
        pending.removeAt(0).run()
        assertNull(preview.markdown)
        pending.removeAt(0).run()
        assertEquals("New translation", preview.markdown)
    }

    fun testCacheReadUsesPooledThread() {
        val file = file("background.md", "source")
        val store = PanelCacheStore()
        val preview = PanelPreview()
        store.entries[file] = entry("source", "Cached")
        val panel = GentransToolWindowPanel(project, store, preview, service())
        Disposer.register(testRootDisposable, panel)

        panel.selectFile(file)
        PlatformTestUtil.waitWhileBusy { preview.markdown != "Cached" }

        assertEquals(false, store.lastReadOnEdt)
    }

    private fun file(name: String, text: String): VirtualFile = myFixture.addFileToProject(name, text).virtualFile

    private fun entry(fileText: String, translation: String, isSelection: Boolean = false) = CachedTranslation(
        fileHash = sha256(fileText),
        sourceHash = sha256(fileText),
        isSelection = isSelection,
        targetLanguage = "English",
        provider = "openai",
        model = "gpt-4o-mini",
        translatedText = translation,
        generation = 7,
    )

    private fun service(executor: ScriptedPromptExecutor = ScriptedPromptExecutor()): TranslationService =
        TranslationService(project, scope, translatorFactory = { config: TranslatorConfig ->
            createTranslator(config, executorFactory = { executor })
        }, apiKeyStore = FakeApiKeyStore())

    private fun panel(store: PanelCacheStore, preview: PanelPreview, service: TranslationService): GentransToolWindowPanel =
        GentransToolWindowPanel(project, store, preview, service, Executor { it.run() }).also {
            Disposer.register(testRootDisposable, it)
        }
}
