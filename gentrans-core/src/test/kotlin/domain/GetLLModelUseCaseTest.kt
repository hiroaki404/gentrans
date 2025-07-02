package domain

import ai.koog.prompt.llm.LLMProvider
import data.FakeConfigDataSource
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
})
