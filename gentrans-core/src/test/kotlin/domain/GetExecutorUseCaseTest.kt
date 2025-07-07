package domain

import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import data.FakeConfigDataSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import model.EnvConfigs
import model.LocalConfigs

class GetExecutorUseCaseTest : FunSpec({

    context("when option configs are provided, they should take precedence") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(providerKey = "google", apiKey = "env-google-key")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs(providerKey = "openai", apiKey = "local-openai-key")
        )
        val useCase = GetExecutorUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val client = useCase(providerOption = "anthropic", apiKey = "option-anthropic-key")
        client.shouldBeInstanceOf<AnthropicLLMClient>()
    }

    context("when option configs are null, local configs should take precedence") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(providerKey = "google", apiKey = "env-google-key")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs(providerKey = "openai", apiKey = "local-openai-key")
        )
        val useCase = GetExecutorUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val client = useCase(providerOption = null, apiKey = null)
        client.shouldBeInstanceOf<OpenAILLMClient>()
    }

    context("when option and local configs are not provided, env configs should take precedence") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(providerKey = "google", apiKey = "env-google-key")
        )
        val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs()) // Simulate no local configs
        val useCase = GetExecutorUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val client = useCase(providerOption = null, apiKey = null)
        client.shouldBeInstanceOf<GoogleLLMClient>()
    }

    context("when no specific configs are provided, default configs should be used") {
        val envConfigDataSource = FakeConfigDataSource(configs = EnvConfigs())
        val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs())
        val useCase = GetExecutorUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        shouldThrow<IllegalArgumentException> {
            useCase(providerOption = null, apiKey = null)
        }.message shouldBe "Openai API key is required"
    }

    context("when env and local configs have providerKey and option has apiKey, local providerKey and option apiKey should be used") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(providerKey = "google", apiKey = "env-google-key")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs(providerKey = "openai", apiKey = null)
        )
        val useCase = GetExecutorUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val client = useCase(providerOption = null, apiKey = "option-openai-key")
        client.shouldBeInstanceOf<OpenAILLMClient>()
    }

    context("when env config has only apiKey and local config has only providerKey, both should be used") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(providerKey = null, apiKey = "env-google-key")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs(providerKey = "google", apiKey = null)
        )
        val useCase = GetExecutorUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val client = useCase(providerOption = null, apiKey = null)
        client.shouldBeInstanceOf<LLMClient>()
    }

    context("when local config has only apiKey and env config has only providerKey, both should be used") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(providerKey = "anthropic", apiKey = null)
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs(providerKey = null, apiKey = "local-anthropic-key")
        )
        val useCase = GetExecutorUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val client = useCase(providerOption = null, apiKey = null)
        client.shouldBeInstanceOf<AnthropicLLMClient>()
    }

    context("when option has only providerKey and local config has only apiKey, both should be used") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(providerKey = "google", apiKey = "env-google-key")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs(providerKey = null, apiKey = "local-openai-key")
        )
        val useCase = GetExecutorUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val client = useCase(providerOption = "openai", apiKey = null)
        client.shouldBeInstanceOf<LLMClient>()
    }

    context("when unknown provider is provided, should throw IllegalArgumentException") {
        val envConfigDataSource = FakeConfigDataSource(configs = EnvConfigs())
        val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs())
        val useCase = GetExecutorUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        shouldThrow<IllegalArgumentException> {
            useCase(providerOption = "unknown-provider", apiKey = "some-key")
        }.message shouldBe "Unknown provider: unknown-provider"
    }

    context("when provider is provided but API key is null, should throw IllegalArgumentException") {
        val envConfigDataSource = FakeConfigDataSource(configs = EnvConfigs())
        val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs())
        val useCase = GetExecutorUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        shouldThrow<IllegalArgumentException> {
            useCase(providerOption = "google", apiKey = null)
        }.message shouldBe "Google API key is required"
    }
})
