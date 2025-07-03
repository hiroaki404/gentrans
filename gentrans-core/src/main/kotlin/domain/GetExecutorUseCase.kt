package domain

import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import data.ConfigDataSource
import model.DefaultConfigs
import model.EnvConfigs
import model.LocalConfigs
import model.OptionConfigs

class GetExecutorUseCase(
    private val envConfigDataSource: ConfigDataSource,
    private val localConfigDataSource: ConfigDataSource
) {
    operator fun invoke(providerOption: String?, apiKey: String?): LLMClient {
        val optionConfigs = OptionConfigs(
            providerKey = providerOption,
            apiKey = apiKey
        )
        val localConfigs = localConfigDataSource.getConfigs() as LocalConfigs
        val envConfigs = envConfigDataSource.getConfigs() as EnvConfigs
        val defaultConfigs = DefaultConfigs()

        // Determine the final providerKey with priority: option > local > env > default
        val finalProviderKey = optionConfigs.providerKey
            ?: localConfigs.providerKey
            ?: envConfigs.providerKey
            ?: defaultConfigs.providerKey

        // Determine the final apiKey with priority: option > local > env > default
        val finalApiKey = optionConfigs.apiKey
            ?: localConfigs.apiKey
            ?: envConfigs.apiKey

        return getExecutor(finalProviderKey, finalApiKey)
    }
}

private fun getExecutor(providerName: String, apiKey: String?): LLMClient {
    val finalApiKey = apiKey
        ?: throw IllegalArgumentException("${providerName.replaceFirstChar { it.uppercase() }} API key is required")

    return when (providerName) {
        "google" -> GoogleLLMClient(apiKey = finalApiKey)

        "openai" -> OpenAILLMClient(apiKey = finalApiKey)

        "anthropic" -> AnthropicLLMClient(apiKey = finalApiKey)

        else -> throw IllegalArgumentException("Unknown provider: $providerName")
    }
}
