package io.github.hiroaki404.gentrans.plugin.translation

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileWithId
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.hiroaki404.gentrans.plugin.settings.GentransSettings

class TranslationCacheStoreTest : BasePlatformTestCase() {
    fun testPutThenGetRoundTrips() = withFreshSettings {
        val file = file("one.md")
        val store = FileAttributeTranslationCacheStore()
        val entry = entry("translated")

        store.put(file, entry)

        assertEquals(entry, store.get(file))
    }

    fun testLongTranslatedTextRoundTrips() = withFreshSettings {
        val file = file("long.md")
        val store = FileAttributeTranslationCacheStore()
        val entry = entry("あ".repeat(40_000))

        store.put(file, entry)

        assertEquals(entry, store.get(file))
    }

    fun testOverwriteReturnsLatestEntry() = withFreshSettings {
        val file = file("overwrite.md")
        val store = FileAttributeTranslationCacheStore()

        store.put(file, entry("first"))
        val latest = entry("second")
        store.put(file, latest)

        assertEquals(latest, store.get(file))
    }

    fun testFilesDoNotInterfere() = withFreshSettings {
        val first = file("first.md")
        val second = file("second.md")
        val store = FileAttributeTranslationCacheStore()
        val firstEntry = entry("first")
        val secondEntry = entry("second")

        store.put(first, firstEntry)
        store.put(second, secondEntry)

        assertEquals(firstEntry, store.get(first))
        assertEquals(secondEntry, store.get(second))
    }

    fun testClearAllInvalidatesEntriesAndIncrementsGeneration() = withFreshSettings {
        val file = file("clear.md")
        val store = FileAttributeTranslationCacheStore()
        store.put(file, entry("before"))
        val oldGeneration = store.currentGeneration()

        store.clearAll()

        assertEquals(oldGeneration + 1, store.currentGeneration())
        assertEquals(store.currentGeneration(), GentransSettings.getInstance().state.cacheGeneration)
        assertNull(store.get(file))
    }

    fun testLightVirtualFileIsIgnored() = withFreshSettings {
        val file = LightVirtualFile("light.md", "source")
        val store = FileAttributeTranslationCacheStore()

        assertFalse(file is VirtualFileWithId)
        store.put(file, entry("ignored"))

        assertNull(store.get(file))
    }

    fun testSha256AndStaleness() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", sha256("abc"))
        val cached = entry("translated").copy(fileHash = sha256("original"))
        assertFalse(cached.isStale("original"))
        assertTrue(cached.isStale("changed"))
    }

    private fun entry(text: String) = CachedTranslation(
        fileHash = sha256("source"),
        sourceHash = sha256("source"),
        isSelection = false,
        targetLanguage = "Japanese",
        provider = "openai",
        model = "gpt-4o-mini",
        translatedText = text,
        generation = GentransSettings.getInstance().state.cacheGeneration,
    )

    private fun file(name: String): VirtualFile = myFixture.addFileToProject(name, "source").virtualFile.also {
        assertTrue("Test file must have a persistent VFS id", it is VirtualFileWithId)
    }

    private inline fun withFreshSettings(block: () -> Unit) {
        val settings = GentransSettings.getInstance()
        val original = settings.state
        try {
            settings.loadState(GentransSettings.State(cacheGeneration = 0))
            block()
        } finally {
            settings.loadState(original)
        }
    }
}
