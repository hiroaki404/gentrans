package domain

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import data.ConfigDataSource
import data.EnvConfigDataSource
import data.LocalConfigDataSource
import model.DefaultConfigs
import model.OptionConfigs

class GetLLModelUseCase(
    private val envConfigDataSource: ConfigDataSource = EnvConfigDataSource(),
    private val localConfigDataSource: ConfigDataSource = LocalConfigDataSource()
) {
    operator fun invoke(llModelOption: String?, providerOption: String?): LLModel {
        val optionConfigs = OptionConfigs(
            llmModelKey = llModelOption,
            providerKey = providerOption
        )
        val localConfigs = localConfigDataSource.getConfigs()
        val envConfigs = envConfigDataSource.getConfigs()
        val defaultConfigs = DefaultConfigs()

        // Determine the final llmModelKey with priority: option > local > env > default
        val finalLlmModelKey = optionConfigs.llmModelKey
            ?: localConfigs.llmModelKey
            ?: envConfigs.llmModelKey
            ?: defaultConfigs.llmModelKey

        // Determine the final providerKey with priority: option > local > env > default
        val finalProviderKey = optionConfigs.providerKey
            ?: localConfigs.providerKey
            ?: envConfigs.providerKey
            ?: defaultConfigs.providerKey

        return getLLModel(finalLlmModelKey, finalProviderKey)
    }
}

private fun getLLModel(llModelName: String, providerName: String): LLModel {
    return LLModel(
        provider = getProvider(providerName),
        id = llModelName,
        capabilities = listOf(
            LLMCapability.Completion,
        ),
        // FIXME: Due to the following changes, it is necessary to specify the appropriate contentLength for each model.
        // https://github.com/JetBrains/koog/pull/438
        contextLength = 1_047_57,
    )
}

private fun getProvider(providerName: String): LLMProvider {
    return when (providerName) {
        "google" -> LLMProvider.Google
        "openai" -> LLMProvider.OpenAI
        "anthropic" -> LLMProvider.Anthropic
        "meta" -> LLMProvider.Meta
        "alibaba" -> LLMProvider.Alibaba
        "openrouter" -> LLMProvider.OpenRouter
        "ollama" -> LLMProvider.Ollama
        else -> throw IllegalArgumentException("Unknown provider: $providerName")
    }
}
