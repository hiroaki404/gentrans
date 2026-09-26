package io.github.hiroaki404.gentrans.cli.config

class FakeConfigDataSource(private val configs: Configs) : ConfigDataSource {
    override fun getConfigs(): Configs {
        return configs
    }
}
