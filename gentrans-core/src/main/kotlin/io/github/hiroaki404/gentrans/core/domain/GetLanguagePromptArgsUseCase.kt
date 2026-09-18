package io.github.hiroaki404.gentrans.core.domain

import io.github.hiroaki404.gentrans.core.data.ConfigDataSource
import io.github.hiroaki404.gentrans.core.data.EnvConfigDataSource
import io.github.hiroaki404.gentrans.core.data.LocalConfigDataSource
import io.github.hiroaki404.gentrans.core.model.DefaultConfigs
import io.github.hiroaki404.gentrans.core.model.LanguagePromptArgs

public class GetLanguagePromptArgsUseCase(
    private val envConfigDataSource: ConfigDataSource = EnvConfigDataSource(),
    private val localConfigDataSource: ConfigDataSource = LocalConfigDataSource()
) {
    public operator fun invoke(targetLanguageOption: String?): LanguagePromptArgs {
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
