package data

import model.Configs

class ConfigRepository(
    private val envConfigDataSource: ConfigDataSource,
    private val localConfigDataSource: ConfigDataSource,
) {
    fun getConfigs(): List<Configs> {
        return listOf(
            envConfigDataSource.getConfigs(),
            localConfigDataSource.getConfigs(),
        )
    }
}
