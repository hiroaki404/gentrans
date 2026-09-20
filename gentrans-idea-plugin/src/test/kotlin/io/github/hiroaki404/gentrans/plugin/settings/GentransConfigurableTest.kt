package io.github.hiroaki404.gentrans.plugin.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase

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
}
