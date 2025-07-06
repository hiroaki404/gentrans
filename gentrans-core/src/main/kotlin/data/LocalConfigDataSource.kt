package data

import model.Configs
import model.LocalConfigs

class LocalConfigDataSource : ConfigDataSource {
    override fun getConfigs(): Configs {
        // TODO: Implement logic to read local configuration from a file or other source.
        return LocalConfigs(providerKey = null, apiKey = null)
    }
}
