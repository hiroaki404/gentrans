package io.github.hiroaki404.gentrans.core.data

import io.github.hiroaki404.gentrans.core.model.Configs

class FakeConfigDataSource(private val configs: Configs) : ConfigDataSource {
    override fun getConfigs(): Configs {
        return configs
    }
}
