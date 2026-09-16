package io.github.hiroaki404.gentrans.core.data

import io.github.hiroaki404.gentrans.core.model.Configs
import io.github.hiroaki404.gentrans.core.model.EnvConfigs

class EnvConfigDataSource : ConfigDataSource {
    override fun getConfigs(): Configs {
        return EnvConfigs(
            llmModelKey = System.getenv("GENTRANS_MODEL"),
            providerKey = System.getenv("GENTRANS_PROVIDER"),
            nativeLanguage = System.getenv("GENTRANS_NATIVE_LANGUAGE"),
            secondLanguage = System.getenv("GENTRANS_SECOND_LANGUAGE"),
            apiKey = System.getenv("GENTRANS_API_KEY")
        )
    }
}
