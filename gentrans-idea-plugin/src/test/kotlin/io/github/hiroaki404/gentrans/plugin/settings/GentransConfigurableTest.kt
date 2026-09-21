package io.github.hiroaki404.gentrans.plugin.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.Container
import javax.swing.JComboBox
import javax.swing.JComponent
import org.junit.Assert.assertEquals

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
}
