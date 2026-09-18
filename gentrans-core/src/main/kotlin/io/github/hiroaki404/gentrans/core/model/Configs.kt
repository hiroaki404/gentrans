package io.github.hiroaki404.gentrans.core.model

public sealed interface Configs {
    public val llmModelKey: String?
    public val providerKey: String?
    public val nativeLanguage: String?
    public val secondLanguage: String?
}

public data class EnvConfigs(
    override val llmModelKey: String? = null,
    override val providerKey: String? = null,
    override val nativeLanguage: String? = null,
    override val secondLanguage: String? = null,
    val apiKey: String? = null,
) : Configs

public data class LocalConfigs(
    override val llmModelKey: String? = null,
    override val providerKey: String? = null,
    override val nativeLanguage: String? = null,
    override val secondLanguage: String? = null,
    val apiKey: String? = null,
) : Configs

public data class OptionConfigs(
    override val llmModelKey: String? = null,
    override val providerKey: String? = null,
    override val nativeLanguage: String? = null,
    override val secondLanguage: String? = null,
    val apiKey: String? = null,
) : Configs

public data class DefaultConfigs(
    override val llmModelKey: String = "gpt-4o",
    override val providerKey: String = "openai",
    override val nativeLanguage: String = "English",
    override val secondLanguage: String = "English",
) : Configs
