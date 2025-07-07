package data

import model.Configs

class FakeConfigDataSource(private val configs: Configs) : ConfigDataSource {
    override fun getConfigs(): Configs {
        return configs
    }
}
