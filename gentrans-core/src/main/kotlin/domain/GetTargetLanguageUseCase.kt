package domain

import data.ConfigDataSource
import data.EnvConfigDataSource
import data.LocalConfigDataSource
import model.DefaultConfigs
import model.OptionConfigs

class GetTargetLanguageUseCase(
    private val envConfigDataSource: ConfigDataSource = EnvConfigDataSource(),
    private val localConfigDataSource: ConfigDataSource = LocalConfigDataSource()
) {
    operator fun invoke(targetLanguageOption: String?): String {
        val optionConfigs = OptionConfigs(
            targetLanguage = targetLanguageOption
        )
        val localConfigs = localConfigDataSource.getConfigs()
        val envConfigs = envConfigDataSource.getConfigs()
        val defaultConfigs = DefaultConfigs()

        // Determine the final targetLanguage with priority: option > local > env > default
        return optionConfigs.targetLanguage
            ?: localConfigs.targetLanguage
            ?: envConfigs.targetLanguage
            ?: defaultConfigs.targetLanguage
    }
}