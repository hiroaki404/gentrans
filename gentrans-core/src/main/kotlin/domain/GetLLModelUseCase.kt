package domain

import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.llm.LLModel
import data.ConfigDataSource
import data.EnvConfigDataSource
import data.LocalConfigDataSource
import model.DefaultConfigs
import model.OptionConfigs

class GetLLModelUseCase(
    private val envConfigDataSource: ConfigDataSource = EnvConfigDataSource(),
    private val localConfigDataSource: ConfigDataSource = LocalConfigDataSource(),
) {
    operator fun invoke(llModelOption: String?, providerOption: String?): LLModel {
        val optionConfigs = OptionConfigs(
            llmModelKey = llModelOption,
            providerKey = providerOption
        )
        val defaultConfigs = DefaultConfigs()

        return OpenAIModels.Chat.GPT4o
    }
}
