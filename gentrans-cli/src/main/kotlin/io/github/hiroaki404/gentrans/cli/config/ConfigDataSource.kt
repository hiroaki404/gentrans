package io.github.hiroaki404.gentrans.cli.config

interface ConfigDataSource {
    fun getConfigs(): Configs
}
