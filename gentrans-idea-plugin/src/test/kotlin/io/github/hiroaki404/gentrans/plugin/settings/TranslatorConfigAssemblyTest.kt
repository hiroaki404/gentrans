package io.github.hiroaki404.gentrans.plugin.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.hiroaki404.gentrans.core.api.Provider
import io.github.hiroaki404.gentrans.core.api.TranslatorConfig
import kotlinx.coroutines.runBlocking

private class FakeApiKeyStore(private val keys: MutableMap<Provider, String?> = mutableMapOf()) : ApiKeyStore {
    override fun get(provider: Provider): String? = keys[provider]

    override fun set(provider: Provider, apiKey: String?) {
        keys[provider] = apiKey
    }
}

class TranslatorConfigAssemblyTest : BasePlatformTestCase() {
    fun testResolvesTheTranslatorConfigAndLooksUpTheStoredApiKey() = runBlocking {
        val state = GentransSettings.State(
            provider = Provider.OpenAI.key,
            model = "gpt-4o",
            targetLanguage = "French",
            nativeLanguage = "Japanese",
            secondLanguage = "English",
        )
        val store = FakeApiKeyStore(mutableMapOf(Provider.OpenAI to "openai-secret"))

        val resolved = state.resolveConfig(store)

        assertEquals(
            TranslatorConfig(
                provider = Provider.OpenAI,
                model = "gpt-4o",
                apiKey = "openai-secret",
                nativeLanguage = "Japanese",
                secondLanguage = "English",
            ),
            resolved.translatorConfig,
        )
        assertEquals("French", resolved.targetLanguage)
    }

    fun testBlankTargetLanguageFallsBackToNativeAndSecondLanguagePair() = runBlocking {
        val state = GentransSettings.State(targetLanguage = "")

        val resolved = state.resolveConfig(FakeApiKeyStore())

        assertNull(resolved.targetLanguage)
    }

    fun testOllamaNeverLooksUpAnApiKeyEvenIfOneIsStored() = runBlocking {
        val state = GentransSettings.State(provider = Provider.Ollama.key, ollamaBaseUrl = "http://localhost:9999")
        val store = FakeApiKeyStore(mutableMapOf(Provider.Ollama to "leftover-secret"))

        val resolved = state.resolveConfig(store)

        assertNull(resolved.translatorConfig.apiKey)
        assertEquals("http://localhost:9999", resolved.translatorConfig.ollamaBaseUrl)
    }

    fun testBlankOllamaBaseUrlFallsBackToTheCoreDefault() = runBlocking {
        val state = GentransSettings.State(provider = Provider.Ollama.key, ollamaBaseUrl = "")

        val resolved = state.resolveConfig(FakeApiKeyStore())

        assertEquals(TranslatorConfig.DEFAULT_OLLAMA_BASE_URL, resolved.translatorConfig.ollamaBaseUrl)
    }

    fun testAStoredProviderThatIsNotSupportedFallsBackToOpenAi() = runBlocking {
        val state = GentransSettings.State(provider = "meta")

        val resolved = state.resolveConfig(FakeApiKeyStore())

        assertEquals(Provider.OpenAI, resolved.translatorConfig.provider)
    }

    fun testAnUnknownStoredProviderKeyFallsBackToOpenAi() = runBlocking {
        val state = GentransSettings.State(provider = "not-a-real-provider")

        val resolved = state.resolveConfig(FakeApiKeyStore())

        assertEquals(Provider.OpenAI, resolved.translatorConfig.provider)
    }
}
