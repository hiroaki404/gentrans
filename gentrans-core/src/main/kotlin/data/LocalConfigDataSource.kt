package data

import model.Configs
import model.LocalConfigs

class LocalConfigDataSource : ConfigDataSource {
    override fun getConfigs(): Configs {
        return LocalConfigs(providerKey = null, apiKey = null)
    }
}
