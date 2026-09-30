package io.github.hiroaki404.gentrans.plugin.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GentransSettingsTest : BasePlatformTestCase() {
    fun testLoadStateRoundTripsEveryField() {
        val saved = GentransSettings.State(
            provider = "anthropic",
            model = "claude-sonnet-4-0",
            targetLanguage = "French",
            nativeLanguage = "Japanese",
            secondLanguage = "English",
            ollamaBaseUrl = "http://localhost:12345",
            cacheGeneration = 42,
        )

        val settings = GentransSettings()
        settings.loadState(saved)

        assertEquals(saved, settings.getState())
    }

    fun testGetInstanceReturnsTheSameApplicationLevelService() {
        assertSame(GentransSettings.getInstance(), GentransSettings.getInstance())
    }
}
