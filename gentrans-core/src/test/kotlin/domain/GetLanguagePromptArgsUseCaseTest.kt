package domain

import data.FakeConfigDataSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import model.EnvConfigs
import model.LocalConfigs

class GetLanguagePromptArgsUseCaseTest : FunSpec({

    context("when target language option is provided, it should be used directly") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(nativeLanguage = "Japanese", secondLanguage = "English")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs()
        )
        val useCase = GetLanguagePromptArgsUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val result = useCase(targetLanguageOption = "French")

        result.targetLanguage shouldBe "French"
        result.nativeLanguage shouldBe "Japanese"
        result.secondLanguage shouldBe "English"
    }

    context("when no target language option is provided, native and second languages should be configured for auto-translation") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(nativeLanguage = "Japanese", secondLanguage = "English")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs()
        )
        val useCase = GetLanguagePromptArgsUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val result = useCase(targetLanguageOption = null)

        result.targetLanguage shouldBe null
        result.nativeLanguage shouldBe "Japanese"
        result.secondLanguage shouldBe "English"
    }

    context("when no languages are configured, defaults should be used") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs()
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs()
        )
        val useCase = GetLanguagePromptArgsUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val result = useCase(targetLanguageOption = null)

        result.targetLanguage shouldBe null
        result.nativeLanguage shouldBe "English"
        result.secondLanguage shouldBe "English"
    }

    context("local config should take precedence over env config") {
        val envConfigDataSource = FakeConfigDataSource(
            configs = EnvConfigs(nativeLanguage = "Japanese", secondLanguage = "English")
        )
        val localConfigDataSource = FakeConfigDataSource(
            configs = LocalConfigs(nativeLanguage = "Korean", secondLanguage = "Chinese")
        )
        val useCase = GetLanguagePromptArgsUseCase(
            envConfigDataSource = envConfigDataSource,
            localConfigDataSource = localConfigDataSource
        )

        val result = useCase(targetLanguageOption = null)

        result.nativeLanguage shouldBe "Korean"
        result.secondLanguage shouldBe "Chinese"
    }
})
