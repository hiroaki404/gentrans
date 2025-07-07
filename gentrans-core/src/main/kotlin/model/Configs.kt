package model

sealed interface Configs {
    val llmModelKey: String?
    val providerKey: String?
}

data class EnvConfigs(
    override val llmModelKey: String? = null,
    override val providerKey: String? = null,
    val apiKey: String? = null,
) : Configs

data class LocalConfigs(
    override val llmModelKey: String? = null,
    override val providerKey: String? = null,
    val apiKey: String? = null,
) : Configs

data class OptionConfigs(
    override val llmModelKey: String? = null,
    override val providerKey: String? = null,
    val apiKey: String? = null,
) : Configs

data class DefaultConfigs(
    override val llmModelKey: String = "gpt-4o",
    override val providerKey: String = "openai",
) : Configs
