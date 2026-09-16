package io.github.hiroaki404.gentrans.core.data

import io.github.hiroaki404.gentrans.core.model.Configs

interface ConfigDataSource {
    fun getConfigs(): Configs
}
