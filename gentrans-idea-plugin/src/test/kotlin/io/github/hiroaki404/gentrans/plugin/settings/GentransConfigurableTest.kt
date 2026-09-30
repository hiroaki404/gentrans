package io.github.hiroaki404.gentrans.plugin.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.openapi.vfs.VirtualFile
import io.github.hiroaki404.gentrans.plugin.GentransBundle
import io.github.hiroaki404.gentrans.plugin.translation.CachedTranslation
import io.github.hiroaki404.gentrans.plugin.translation.TranslationCacheStore
import org.junit.Assert.assertEquals
import java.awt.Container
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JLabel

class GentransConfigurableTest : BasePlatformTestCase() {
    fun testCreatingAndResettingThePanelDoesNotThrowWhenTheStoredProviderIsUnsupported() {
        val settings = GentransSettings.getInstance()
        val original = settings.getState()
        try {
            settings.loadState(GentransSettings.State(provider = "meta"))
            val configurable = GentransConfigurable()
            configurable.createComponent()
            configurable.reset()
        } finally {
            settings.loadState(original)
        }
    }

    fun testApplyingAChangedProviderPersistsTheSelectedProvider() {
        val settings = GentransSettings.getInstance()
        val original = settings.getState()
        try {
            settings.loadState(GentransSettings.State(provider = "openai"))
            val configurable = GentransConfigurable()
            val component = configurable.createComponent()
            val providerComboBox = findProviderComboBox(component)

            providerComboBox.selectedItem = "google"
            configurable.apply()

            assertEquals("google", settings.state.provider)
        } finally {
            settings.loadState(original)
        }
    }

    fun testClearingCacheDoesNotModifySettings() {
        val store = FakeTranslationCacheStore()
        val configurable = GentransConfigurable(store)
        val component = configurable.createComponent()
        configurable.reset()

        assertFalse(configurable.isModified())
        findComponent<JButton>(component) {
            it.text == GentransBundle.message("gentrans.settings.clearCache")
        }.doClick()

        assertEquals(1, store.clearCount)
        assertFalse(configurable.isModified())
        assertTrue(findComponent<JLabel>(component) {
            it.text == GentransBundle.message("gentrans.settings.cacheCleared")
        }.isVisible)
    }

    fun testApplyAndResetPreserveCacheGeneration() {
        val settings = GentransSettings.getInstance()
        val original = settings.state
        try {
            settings.loadState(GentransSettings.State(cacheGeneration = 4))
            val configurable = GentransConfigurable(FakeTranslationCacheStore())
            configurable.createComponent()
            configurable.reset()
            settings.state.cacheGeneration = 5

            configurable.apply()
            configurable.reset()

            assertEquals(5, settings.state.cacheGeneration)
        } finally {
            settings.loadState(original)
        }
    }

    private fun findProviderComboBox(component: JComponent): JComboBox<*> {
        if (component is JComboBox<*>) return component
        if (component is Container) {
            component.components.forEach { child ->
                if (child is JComponent) {
                    runCatching { return findProviderComboBox(child) }
                }
            }
        }
        error("Provider combo box not found")
    }

    private inline fun <reified T : JComponent> findComponent(
        component: JComponent,
        predicate: (T) -> Boolean,
    ): T = allComponents(component).filterIsInstance<T>().firstOrNull(predicate)
        ?: error("${T::class.simpleName} not found")

    private fun allComponents(component: JComponent): Sequence<JComponent> = sequence {
        yield(component)
        if (component is Container) {
            component.components.filterIsInstance<JComponent>().forEach { yieldAll(allComponents(it)) }
        }
    }

    private class FakeTranslationCacheStore : TranslationCacheStore {
        var clearCount = 0

        override fun get(file: VirtualFile): CachedTranslation? = null
        override fun put(file: VirtualFile, entry: CachedTranslation) = Unit
        override fun clearAll() { clearCount++ }
        override fun currentGeneration(): Long = 0
    }
}
