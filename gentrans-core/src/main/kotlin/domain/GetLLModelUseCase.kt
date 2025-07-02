package domain

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import data.ConfigDataSource
import model.DefaultConfigs
import model.OptionConfigs

class GetLLModelUseCase(
    private val envConfigDataSource: ConfigDataSource,
    private val localConfigDataSource: ConfigDataSource
) {
    operator fun invoke(llModelOption: String?, providerOption: String?): LLModel {
        val optionConfigs = OptionConfigs(
            llmModelKey = llModelOption,
            providerKey = providerOption
        )
        val defaultConfigs = DefaultConfigs()

        // Option configurations have the highest priority
        if (llModelOption != null && providerOption != null) {
            return getLLModel(optionConfigs.llmModelKey!!, optionConfigs.providerKey!!)
        }

        // Local configurations have the next priority
        val localConfigs = localConfigDataSource.getConfigs()
        if (localConfigs.llmModelKey != null && localConfigs.providerKey != null) {
            return getLLModel(localConfigs.llmModelKey!!, localConfigs.providerKey!!)
        }

        // Environment configurations have the next priority
        val envConfigs = envConfigDataSource.getConfigs()
        if (envConfigs.llmModelKey != null && envConfigs.providerKey != null) {
            return getLLModel(envConfigs.llmModelKey!!, envConfigs.providerKey!!)
        }

        // If no configurations are provided, use default values
        return getLLModel(defaultConfigs.llmModelKey, defaultConfigs.providerKey)
    }
}

private fun getLLModel(llModelName: String, providerName: String): LLModel {
    return LLModel(
        provider = getProvider(providerName),
        id = llModelName,
        capabilities = listOf(
            LLMCapability.Temperature,
            LLMCapability.Schema.JSON.Simple,
            LLMCapability.Tools
        ),
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
