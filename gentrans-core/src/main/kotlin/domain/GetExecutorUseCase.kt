package domain

import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import data.ConfigDataSource
import model.DefaultConfigs
import model.OptionConfigs

class GetExecutorUseCase(
    private val envConfigDataSource: ConfigDataSource,
    private val localConfigDataSource: ConfigDataSource
) {
    operator fun invoke(providerOption: String?, apiKey: String?): PromptExecutor {
        val optionConfigs = OptionConfigs(
            providerKey = providerOption,
            apiKey = apiKey
        )
        val localConfigs = localConfigDataSource.getConfigs()
        val envConfigs = envConfigDataSource.getConfigs()
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
            ?: defaultConfigs.apiKey

        return getExecutor(finalProviderKey, finalApiKey)
    }
}

private fun getExecutor(providerName: String, apiKey: String?): PromptExecutor {
    return when (providerName) {
        "google" -> simpleGoogleAIExecutor(apiKey ?: throw IllegalArgumentException("Google API key is required"))
        "openai" -> simpleOpenAIExecutor(apiKey ?: throw IllegalArgumentException("OpenAI API key is required"))
        "anthropic" -> simpleAnthropicExecutor(apiKey ?: throw IllegalArgumentException("Anthropic API key is required"))
        else -> throw IllegalArgumentException("Unknown provider: $providerName")
    }
}
