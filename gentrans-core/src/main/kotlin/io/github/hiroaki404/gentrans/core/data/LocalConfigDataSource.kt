package io.github.hiroaki404.gentrans.core.data

import io.github.hiroaki404.gentrans.core.model.Configs
import io.github.hiroaki404.gentrans.core.model.LocalConfigs

class LocalConfigDataSource : ConfigDataSource {
    override fun getConfigs(): Configs {
        // TODO: Implement logic to read local configuration from a file or other source.
        return LocalConfigs(providerKey = null, apiKey = null)
    }
}
