package io.github.hiroaki404.gentrans.core.data

import io.github.hiroaki404.gentrans.core.model.Configs

public interface ConfigDataSource {
    public fun getConfigs(): Configs
}
