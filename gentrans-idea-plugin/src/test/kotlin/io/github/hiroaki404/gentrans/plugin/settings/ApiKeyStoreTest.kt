package io.github.hiroaki404.gentrans.plugin.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.hiroaki404.gentrans.core.api.Provider

class ApiKeyStoreTest : BasePlatformTestCase() {
    fun testStoredKeysRoundTripPerProvider() {
        val store = PasswordSafeApiKeyStore()
        store.set(Provider.OpenAI, "openai-secret")
        store.set(Provider.Anthropic, "anthropic-secret")

        assertEquals("openai-secret", store.get(Provider.OpenAI))
        assertEquals("anthropic-secret", store.get(Provider.Anthropic))
    }

    fun testBlankApiKeyClearsTheStoredCredential() {
        val store = PasswordSafeApiKeyStore()
        store.set(Provider.Google, "google-secret")
        store.set(Provider.Google, "")

        assertNull(store.get(Provider.Google))
    }
}
