package data

import model.Configs
import model.EnvConfigs

class EnvConfigDataSource : ConfigDataSource {
    override fun getConfigs(): Configs {
        return EnvConfigs(
            llmModelKey = System.getenv("GENTRANS_MODEL"),
            providerKey = System.getenv("GENTRANS_PROVIDER"),
            apiKey = System.getenv("GENTRANS_API_KEY")
        )
    }
}
