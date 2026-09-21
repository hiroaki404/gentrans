package io.github.hiroaki404.gentrans.plugin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import io.github.hiroaki404.gentrans.core.api.Provider
import io.github.hiroaki404.gentrans.plugin.GentransBundle
import javax.swing.JPasswordField

internal class GentransConfigurable : BoundConfigurable(GentransBundle.message("gentrans.settings.displayName")) {
    private val settings get() = GentransSettings.getInstance().state
    private val apiKeyStore: ApiKeyStore = PasswordSafeApiKeyStore()
    private val apiKeyField = JPasswordField(40)

    private var currentProvider = resolveSupportedProviderOrDefault(settings.provider)
    private val loadedApiKeys = mutableMapOf<Provider, String>()
    private val editedApiKeys = mutableMapOf<Provider, String>()

    private lateinit var apiKeyRow: Row
    private lateinit var ollamaBaseUrlRow: Row

    override fun createPanel(): DialogPanel {
        loadApiKeyAsync(currentProvider)

        val result = panel {
            row(GentransBundle.message("gentrans.settings.provider")) {
                comboBox(SupportedProviders.map { it.key })
                    .bindItem({ settings.provider }, { key -> settings.provider = key ?: settings.provider })
                    .applyToComponent {
                        addActionListener {
                            val selectedKey = selectedItem as? String ?: return@addActionListener
                            val selected = Provider.fromKeyOrNull(selectedKey) ?: return@addActionListener
                            onProviderSelected(selected)
                        }
                    }
            }
            row(GentransBundle.message("gentrans.settings.model")) {
                textField().bindText(settings::model).columns(20)
            }
            apiKeyRow = row(GentransBundle.message("gentrans.settings.apiKey")) {
                cell(apiKeyField)
            }
            row(GentransBundle.message("gentrans.settings.targetLanguage")) {
                textField().bindText(settings::targetLanguage)
                    .comment(GentransBundle.message("gentrans.settings.targetLanguage.comment"))
                    .columns(20)
            }
            row(GentransBundle.message("gentrans.settings.nativeLanguage")) {
                textField().bindText(settings::nativeLanguage).columns(20)
            }
            row(GentransBundle.message("gentrans.settings.secondLanguage")) {
                textField().bindText(settings::secondLanguage).columns(20)
            }
            ollamaBaseUrlRow = row(GentransBundle.message("gentrans.settings.ollamaBaseUrl")) {
                textField().bindText(settings::ollamaBaseUrl).columns(20)
            }
        }
        applyProviderVisibility()
        return result
    }

    override fun isModified(): Boolean =
        super.isModified() || String(apiKeyField.password) != loadedApiKeys[currentProvider].orEmpty()

    override fun reset() {
        // Recompute before super.reset() so the comboBox binding's getter sees the fresh value.
        currentProvider = resolveSupportedProviderOrDefault(settings.provider)
        super.reset()
        editedApiKeys.clear()
        apiKeyField.text = loadedApiKeys[currentProvider].orEmpty()
        applyProviderVisibility()
    }

    override fun apply() {
        super.apply()
        editedApiKeys[currentProvider] = String(apiKeyField.password)
        editedApiKeys.forEach { (provider, apiKey) ->
            if (apiKey != loadedApiKeys[provider].orEmpty()) {
                saveApiKeyAsync(provider, apiKey)
                loadedApiKeys[provider] = apiKey
            }
        }
        editedApiKeys.clear()
    }

    private fun onProviderSelected(provider: Provider) {
        if (provider == currentProvider) return
        editedApiKeys[currentProvider] = String(apiKeyField.password)
        currentProvider = provider
        apiKeyField.text = editedApiKeys[provider] ?: loadedApiKeys[provider].orEmpty()
        loadApiKeyAsync(provider)
        applyProviderVisibility()
    }

    private fun applyProviderVisibility() {
        apiKeyRow.enabled(currentProvider != Provider.Ollama)
        ollamaBaseUrlRow.visible(currentProvider == Provider.Ollama)
    }

    // Off the EDT via executeOnPooledThread; ApiKeyStore itself is a plain blocking call
    // (PasswordSafe's own API is blocking), so no runBlocking is needed here.
    private fun loadApiKeyAsync(provider: Provider) {
        if (provider == Provider.Ollama || loadedApiKeys.containsKey(provider)) return
        val application = ApplicationManager.getApplication()
        application.executeOnPooledThread {
            val apiKey = apiKeyStore.get(provider).orEmpty()
            application.invokeLater({
                loadedApiKeys[provider] = apiKey
                if (provider == currentProvider && apiKeyField.password.isEmpty()) {
                    apiKeyField.text = apiKey
                }
            }, ModalityState.any())
        }
    }

    private fun saveApiKeyAsync(provider: Provider, apiKey: String) {
        val application = ApplicationManager.getApplication()
        application.executeOnPooledThread {
            apiKeyStore.set(provider, apiKey)
        }
    }
}
