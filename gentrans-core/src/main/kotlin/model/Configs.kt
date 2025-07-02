package model

sealed interface Configs {
    val llmModelKey: String
    val providerKey: String
}

data class EnvConfigs(
    override val llmModelKey: String,
    override val providerKey: String,
) : Configs

data class LocalConfigs(
    override val llmModelKey: String,
    override val providerKey: String,
) : Configs
