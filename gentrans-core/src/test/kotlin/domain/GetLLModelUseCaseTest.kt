package domain

import data.FakeConfigDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import model.EnvConfigs
import model.LocalConfigs

class GetLLModelUseCaseTest : FunSpec({

    test("GetLLModelUseCase should return LLModel based on priority") {

        context("when option configs are provided, they should take precedence") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(llmModelKey = "env_llm", providerKey = "env_provider")
            )
            val localConfigDataSource = FakeConfigDataSource(
                configs = LocalConfigs(llmModelKey = "local_llm", providerKey = "local_provider")
            )
            val useCase = GetLLModelUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val llModel = useCase(llModelOption = "option_llm", providerOption = "option_provider")
            llModel.id shouldBe "option_llm"
            llModel.provider shouldBe "option_provider"
        }

        context("when option configs are null, local configs should take precedence") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(llmModelKey = "env_llm", providerKey = "env_provider")
            )
            val localConfigDataSource = FakeConfigDataSource(
                configs = LocalConfigs(llmModelKey = "local_llm", providerKey = "local_provider")
            )
            val useCase = GetLLModelUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val llModel = useCase(llModelOption = null, providerOption = null)
            llModel.id shouldBe "local_llm"
            llModel.provider shouldBe "local_provider"
        }

        context("when option and local configs are not provided, env configs should take precedence") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(llmModelKey = "env_llm", providerKey = "env_provider")
            )
            val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs()) // Simulate no local configs
            val useCase = GetLLModelUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val llModel = useCase(llModelOption = null, providerOption = null)
            llModel.id shouldBe "env_llm"
            llModel.provider shouldBe "env_provider"
        }

        context("when no specific configs are provided, default configs should be used") {
            val envConfigDataSource = FakeConfigDataSource(configs = EnvConfigs())
            val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs())
            val useCase = GetLLModelUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val llModel = useCase(llModelOption = null, providerOption = null)
            llModel.id shouldBe "gpt-4o" // Assuming default is gpt-4o
            llModel.provider shouldBe "openai" // Assuming default is openai
        }
    }
})
