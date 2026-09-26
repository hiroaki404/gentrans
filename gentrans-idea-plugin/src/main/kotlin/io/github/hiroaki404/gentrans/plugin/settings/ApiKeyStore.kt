package io.github.hiroaki404.gentrans.plugin.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import io.github.hiroaki404.gentrans.core.api.Provider

/**
 * Seam over [PasswordSafe] so tests can substitute an in-memory store. Blocking, matching
 * PasswordSafe's own API - callers own getting off the EDT (see local-docs 1-3).
 */
internal interface ApiKeyStore {
    fun get(provider: Provider): String?

    fun set(provider: Provider, apiKey: String?)
}

internal class PasswordSafeApiKeyStore : ApiKeyStore {
    override fun get(provider: Provider): String? =
        PasswordSafe.instance.getPassword(credentialAttributes(provider))

    override fun set(provider: Provider, apiKey: String?) {
        PasswordSafe.instance.set(
            credentialAttributes(provider),
            apiKey?.takeIf { it.isNotBlank() }?.let { Credentials(null, it) },
        )
    }

    private fun credentialAttributes(provider: Provider) =
        CredentialAttributes(generateServiceName("GenTrans", provider.key))
}
