package io.github.hiroaki404.gentrans.plugin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import io.github.hiroaki404.gentrans.core.api.Provider

@Service(Service.Level.APP)
@State(name = "GentransSettings", storages = [Storage("gentrans.xml")])
internal class GentransSettings : PersistentStateComponent<GentransSettings.State> {
    data class State
    @JvmOverloads
    constructor(
        var provider: String = Provider.OpenAI.key,
        var model: String = "gpt-4o-mini",
        var targetLanguage: String = "",
        var nativeLanguage: String = "Japanese",
        var secondLanguage: String = "English",
        var ollamaBaseUrl: String = "",
        var cacheGeneration: Long = 0,
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): GentransSettings =
            ApplicationManager.getApplication().getService(GentransSettings::class.java)
    }
}
