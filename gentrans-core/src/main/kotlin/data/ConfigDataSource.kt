package data

import model.Configs

interface ConfigDataSource {
    fun getConfigs(): Configs
}
