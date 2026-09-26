package io.github.hiroaki404.gentrans.cli.config

class LocalConfigDataSource : ConfigDataSource {
    override fun getConfigs(): Configs {
        // TODO: Implement logic to read local configuration from a file or other source.
        return LocalConfigs(providerKey = null, apiKey = null)
    }
}
