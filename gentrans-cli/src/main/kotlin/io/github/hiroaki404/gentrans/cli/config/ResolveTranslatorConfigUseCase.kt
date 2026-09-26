package io.github.hiroaki404.gentrans.cli.config

import io.github.hiroaki404.gentrans.core.api.Provider
import io.github.hiroaki404.gentrans.core.api.TranslatorConfig

class ResolveTranslatorConfigUseCase(
    private val envConfigDataSource: ConfigDataSource = EnvConfigDataSource(),
    private val localConfigDataSource: ConfigDataSource = LocalConfigDataSource()
) {
    operator fun invoke(providerOption: String?, modelOption: String?, apiKeyOption: String?): TranslatorConfig {
        val localConfigs = localConfigDataSource.getConfigs() as LocalConfigs
        val envConfigs = envConfigDataSource.getConfigs() as EnvConfigs
        val defaultConfigs = DefaultConfigs()

        val finalProviderKey = providerOption
            ?: localConfigs.providerKey
            ?: envConfigs.providerKey
            ?: defaultConfigs.providerKey

        val finalApiKey = apiKeyOption
            ?: localConfigs.apiKey
            ?: envConfigs.apiKey

        val finalModel = modelOption
            ?: localConfigs.llmModelKey
            ?: envConfigs.llmModelKey
            ?: defaultConfigs.llmModelKey

        val finalNativeLanguage = localConfigs.nativeLanguage
            ?: envConfigs.nativeLanguage
            ?: defaultConfigs.nativeLanguage

        val finalSecondLanguage = localConfigs.secondLanguage
            ?: envConfigs.secondLanguage
            ?: defaultConfigs.secondLanguage

        return TranslatorConfig(
            provider = Provider.fromKey(finalProviderKey),
            model = finalModel,
            apiKey = finalApiKey,
            nativeLanguage = finalNativeLanguage,
            secondLanguage = finalSecondLanguage,
        )
    }
}
