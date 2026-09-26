package io.github.hiroaki404.gentrans.core.api

/**
 * Single source of supported AI providers.
 *
 * Config files, environment variables, CLI options, and plugin settings screens all hold
 * values as strings, so they are converted to this enum via [fromKey] / [fromKeyOrNull] at
 * the boundary, and internal branching is done on the enum.
 */
public enum class Provider(public val key: String, public val displayName: String) {
    Google("google", "Google"),
    OpenAI("openai", "OpenAI"),
    Anthropic("anthropic", "Anthropic"),
    Meta("meta", "Meta"),
    Alibaba("alibaba", "Alibaba"),
    OpenRouter("openrouter", "OpenRouter"),
    Ollama("ollama", "Ollama"),
    ;

    public companion object {
        public fun fromKeyOrNull(key: String): Provider? = entries.firstOrNull { it.key.equals(key, ignoreCase = true) }

        public fun fromKey(key: String): Provider =
            fromKeyOrNull(key) ?: throw IllegalArgumentException("Unknown provider: $key")
    }
}
