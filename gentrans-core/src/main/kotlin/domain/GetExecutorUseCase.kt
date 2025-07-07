package domain

import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.ollama.client.OllamaClient
import data.ConfigDataSource
import data.EnvConfigDataSource
import data.LocalConfigDataSource
import model.DefaultConfigs
import model.EnvConfigs
import model.LocalConfigs
import model.OptionConfigs

class GetExecutorUseCase(
    private val envConfigDataSource: ConfigDataSource = EnvConfigDataSource(),
    private val localConfigDataSource: ConfigDataSource = LocalConfigDataSource()
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
    return when (providerName) {
        "google" -> {
            val finalApiKey = apiKey
                ?: throw IllegalArgumentException("${providerName.replaceFirstChar { it.uppercase() }} API key is required")
            GoogleLLMClient(apiKey = finalApiKey)
        }

        "openai" -> {
            val finalApiKey = apiKey
                ?: throw IllegalArgumentException("${providerName.replaceFirstChar { it.uppercase() }} API key is required")
            OpenAILLMClient(apiKey = finalApiKey)
        }

        "anthropic" -> {
            val finalApiKey = apiKey
                ?: throw IllegalArgumentException("${providerName.replaceFirstChar { it.uppercase() }} API key is required")
            AnthropicLLMClient(apiKey = finalApiKey)
        }

        "ollama" -> OllamaClient()

        else -> throw IllegalArgumentException("Unknown provider: $providerName")
    }
}
