package domain

import data.ConfigDataSource
import data.EnvConfigDataSource
import data.LocalConfigDataSource
import model.DefaultConfigs
import model.LanguagePromptArgs

class GetLanguagePromptArgsUseCase(
    private val envConfigDataSource: ConfigDataSource = EnvConfigDataSource(),
    private val localConfigDataSource: ConfigDataSource = LocalConfigDataSource()
) {
    operator fun invoke(targetLanguageOption: String?): LanguagePromptArgs {
        val localConfigs = localConfigDataSource.getConfigs()
        val envConfigs = envConfigDataSource.getConfigs()
        val defaultConfigs = DefaultConfigs()

        val nativeLanguage = localConfigs.nativeLanguage
            ?: envConfigs.nativeLanguage
            ?: defaultConfigs.nativeLanguage

        val secondLanguage = localConfigs.secondLanguage
            ?: envConfigs.secondLanguage
            ?: defaultConfigs.secondLanguage

        return LanguagePromptArgs(
            targetLanguage = targetLanguageOption,
            nativeLanguage = nativeLanguage,
            secondLanguage = secondLanguage
        )
    }
}
