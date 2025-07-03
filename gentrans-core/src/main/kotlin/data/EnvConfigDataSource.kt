package data

import model.Configs
import model.EnvConfigs

class EnvConfigDataSource : ConfigDataSource {
    override fun getConfigs(): Configs {
        return EnvConfigs()
    }
}
