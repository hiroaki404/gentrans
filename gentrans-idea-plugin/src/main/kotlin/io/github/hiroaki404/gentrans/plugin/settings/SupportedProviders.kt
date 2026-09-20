package io.github.hiroaki404.gentrans.plugin.settings

import io.github.hiroaki404.gentrans.core.api.Provider

// Meta/Alibaba/OpenRouter exist on the core enum but aren't offered in the first release
// (local-docs/10-design-final.md 0 / 2-1).
internal val SupportedProviders: List<Provider> =
    listOf(Provider.OpenAI, Provider.Google, Provider.Anthropic, Provider.Ollama)

/** A stored key that's unknown or one of the withheld providers falls back to [Provider.OpenAI]. */
internal fun resolveSupportedProviderOrDefault(key: String): Provider =
    Provider.fromKeyOrNull(key)?.takeIf { it in SupportedProviders } ?: Provider.OpenAI
