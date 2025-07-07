package domain

import ai.koog.prompt.llm.LLMProvider
import data.FakeConfigDataSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import model.EnvConfigs
import model.LocalConfigs

class GetLLModelUseCaseTest : FunSpec({

    context("when option configs are provided, they should take precedence") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(llmModelKey = "gemini-2.0-flash", providerKey = "google")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs(llmModelKey = "llama-3.2:3b", providerKey = "meta")
        )
        val useCase = GetLLModelUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val llModel = useCase(llModelOption = "claude-sonnet-4-0", providerOption = "anthropic")
        llModel.id shouldBe "claude-sonnet-4-0"
        llModel.provider shouldBe LLMProvider.Anthropic
    }

    context("when option configs are null, local configs should take precedence") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(llmModelKey = "gemini-2.0-flash", providerKey = "google")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs(llmModelKey = "llama-3.2:3b", providerKey = "meta")
        )
        val useCase = GetLLModelUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val llModel = useCase(llModelOption = null, providerOption = null)
        llModel.id shouldBe "llama-3.2:3b"
        llModel.provider shouldBe LLMProvider.Meta
    }

    context("when option and local configs are not provided, env configs should take precedence") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(llmModelKey = "gemini-2.0-flash", providerKey = "google")
        )
        val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs()) // Simulate no local configs
        val useCase = GetLLModelUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val llModel = useCase(llModelOption = null, providerOption = null)
        llModel.id shouldBe "gemini-2.0-flash"
        llModel.provider shouldBe LLMProvider.Google
    }

    context("when no specific configs are provided, default configs should be used") {
        val envConfigDataSource = FakeConfigDataSource(configs = EnvConfigs())
        val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs())
        val useCase = GetLLModelUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val llModel = useCase(llModelOption = null, providerOption = null)
        llModel.id shouldBe "gpt-4o"
        llModel.provider shouldBe LLMProvider.OpenAI
    }

    context("when env and local configs have llmModelKey and option has providerOption, local llmModelKey and option providerOption should be used") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(llmModelKey = "gemini-2.0-flash", providerKey = "google")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs(llmModelKey = "llama-3.2:3b", providerKey = null)
        )
        val useCase = GetLLModelUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val llModel = useCase(llModelOption = null, providerOption = "anthropic")
        llModel.id shouldBe "llama-3.2:3b"
        llModel.provider shouldBe LLMProvider.Anthropic
    }

    context("when env config has only providerKey and local config has only llmModelKey, both should be used") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(llmModelKey = null, providerKey = "google")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs(llmModelKey = "llama-3.2:3b", providerKey = null)
        )
        val useCase = GetLLModelUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val llModel = useCase(llModelOption = null, providerOption = null)
        llModel.id shouldBe "llama-3.2:3b"
        llModel.provider shouldBe LLMProvider.Google
    }

    context("when local config has only providerKey and env config has only llmModelKey, both should be used") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(llmModelKey = "gemini-2.0-flash", providerKey = null)
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs(llmModelKey = null, providerKey = "meta")
        )
        val useCase = GetLLModelUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val llModel = useCase(llModelOption = null, providerOption = null)
        llModel.id shouldBe "gemini-2.0-flash"
        llModel.provider shouldBe LLMProvider.Meta
    }

    context("when option has only llmModelKey and local config has only providerKey, both should be used") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(llmModelKey = "gemini-2.0-flash", providerKey = "google")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs(llmModelKey = null, providerKey = "anthropic")
        )
        val useCase = GetLLModelUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val llModel = useCase(llModelOption = "claude-sonnet-4-0", providerOption = null)
        llModel.id shouldBe "claude-sonnet-4-0"
        llModel.provider shouldBe LLMProvider.Anthropic
    }

    context("when unknown provider is provided, should throw IllegalArgumentException") {
        val envConfigDataSource = FakeConfigDataSource(configs = EnvConfigs())
        val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs())
        val useCase = GetLLModelUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        shouldThrow<IllegalArgumentException> {
            useCase(llModelOption = "gpt-4o", providerOption = "unknown-provider")
        }
    }
})
