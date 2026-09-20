package io.github.hiroaki404.gentrans.core.llm

import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel

internal fun buildLLMClient(
    providerName: String,
    apiKey: String?,
    ollamaBaseUrl: String = OllamaClient.DEFAULT_BASE_URL,
): LLMClient {
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

        "ollama" -> OllamaClient(baseUrl = ollamaBaseUrl)

        else -> throw IllegalArgumentException("Unknown provider: $providerName")
    }
}

internal fun buildLLModel(llModelName: String, providerName: String): LLModel {
    return LLModel(
        provider = getProvider(providerName),
        id = llModelName,
        capabilities = buildList {
            add(LLMCapability.Completion)
            // Koog 1.x: OpenAI client requires an explicit endpoint capability
            if (providerName == "openai" || providerName == "openrouter") {
                add(LLMCapability.OpenAIEndpoint.Completions)
            }
        },
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
