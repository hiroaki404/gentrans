package domain

import data.FakeConfigDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import model.EnvConfigs
import model.LocalConfigs

class GetTargetLanguageUseCaseTest : FunSpec({

    context("when option target language is provided, it should take precedence") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(targetLanguage = "French")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs()
        )
        val useCase = GetTargetLanguageUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val result = useCase(targetLanguageOption = "Japanese")
        result shouldBe "Japanese"
    }

    context("when env target language is provided, it should take precedence over default") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(targetLanguage = "Spanish")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs()
        )
        val useCase = GetTargetLanguageUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val result = useCase(targetLanguageOption = null)
        result shouldBe "Spanish"
    }

    context("when no target language is provided, default English should be used") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs()
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs()
        )
        val useCase = GetTargetLanguageUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val result = useCase(targetLanguageOption = null)
        result shouldBe "English"
    }

})