package io.github.hiroaki404.gentrans.cli.config

import io.github.hiroaki404.gentrans.core.api.Provider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ResolveTranslatorConfigUseCaseTest : FunSpec({

    context("provider resolution") {
        test("when option configs are provided, they should take precedence") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(providerKey = "google", apiKey = "env-google-key")
            )
            val localConfigDataSource = FakeConfigDataSource(
                configs = LocalConfigs(providerKey = "openai", apiKey = "local-openai-key")
            )
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = "anthropic", modelOption = null, apiKeyOption = "option-anthropic-key")
            config.provider shouldBe Provider.Anthropic
            config.apiKey shouldBe "option-anthropic-key"
        }

        test("when option configs are null, local configs should take precedence") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(providerKey = "google", apiKey = "env-google-key")
            )
            val localConfigDataSource = FakeConfigDataSource(
                configs = LocalConfigs(providerKey = "openai", apiKey = "local-openai-key")
            )
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = null, modelOption = null, apiKeyOption = null)
            config.provider shouldBe Provider.OpenAI
            config.apiKey shouldBe "local-openai-key"
        }

        test("when option and local configs are not provided, env configs should take precedence") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(providerKey = "google", apiKey = "env-google-key")
            )
            val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs())
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = null, modelOption = null, apiKeyOption = null)
            config.provider shouldBe Provider.Google
            config.apiKey shouldBe "env-google-key"
        }

        test("when no specific configs are provided, default configs should be used") {
            val envConfigDataSource = FakeConfigDataSource(configs = EnvConfigs())
            val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs())
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = null, modelOption = null, apiKeyOption = null)
            config.provider shouldBe Provider.OpenAI
            config.apiKey shouldBe null
        }

        test("when env and local configs have providerKey and option has apiKey, local providerKey and option apiKey should be used") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(providerKey = "google", apiKey = "env-google-key")
            )
            val localConfigDataSource = FakeConfigDataSource(
                configs = LocalConfigs(providerKey = "openai", apiKey = null)
            )
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = null, modelOption = null, apiKeyOption = "option-openai-key")
            config.provider shouldBe Provider.OpenAI
            config.apiKey shouldBe "option-openai-key"
        }

        test("when env config has only apiKey and local config has only providerKey, both should be used") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(providerKey = null, apiKey = "env-google-key")
            )
            val localConfigDataSource = FakeConfigDataSource(
                configs = LocalConfigs(providerKey = "google", apiKey = null)
            )
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = null, modelOption = null, apiKeyOption = null)
            config.provider shouldBe Provider.Google
            config.apiKey shouldBe "env-google-key"
        }

        test("when local config has only apiKey and env config has only providerKey, both should be used") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(providerKey = "anthropic", apiKey = null)
            )
            val localConfigDataSource = FakeConfigDataSource(
                configs = LocalConfigs(providerKey = null, apiKey = "local-anthropic-key")
            )
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = null, modelOption = null, apiKeyOption = null)
            config.provider shouldBe Provider.Anthropic
            config.apiKey shouldBe "local-anthropic-key"
        }

        test("when option has only providerKey and local config has only apiKey, both should be used") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(providerKey = "google", apiKey = "env-google-key")
            )
            val localConfigDataSource = FakeConfigDataSource(
                configs = LocalConfigs(providerKey = null, apiKey = "local-openai-key")
            )
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = "openai", modelOption = null, apiKeyOption = null)
            config.provider shouldBe Provider.OpenAI
            config.apiKey shouldBe "local-openai-key"
        }

        test("when unknown provider is provided, should throw IllegalArgumentException") {
            val envConfigDataSource = FakeConfigDataSource(configs = EnvConfigs())
            val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs())
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            shouldThrow<IllegalArgumentException> {
                useCase(providerOption = "unknown-provider", modelOption = null, apiKeyOption = "some-key")
            }.message shouldBe "Unknown provider: unknown-provider"
        }
    }

    context("model resolution") {
        test("when option configs are provided, they should take precedence") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(llmModelKey = "gemini-2.0-flash", providerKey = "google")
            )
            val localConfigDataSource = FakeConfigDataSource(
                configs = LocalConfigs(llmModelKey = "llama-3.2:3b", providerKey = "meta")
            )
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = "anthropic", modelOption = "claude-sonnet-4-0", apiKeyOption = null)
            config.model shouldBe "claude-sonnet-4-0"
            config.provider shouldBe Provider.Anthropic
        }

        test("when option configs are null, local configs should take precedence") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(llmModelKey = "gemini-2.0-flash", providerKey = "google")
            )
            val localConfigDataSource = FakeConfigDataSource(
                configs = LocalConfigs(llmModelKey = "llama-3.2:3b", providerKey = "meta")
            )
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = null, modelOption = null, apiKeyOption = null)
            config.model shouldBe "llama-3.2:3b"
            config.provider shouldBe Provider.Meta
        }

        test("when option and local configs are not provided, env configs should take precedence") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(llmModelKey = "gemini-2.0-flash", providerKey = "google")
            )
            val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs())
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = null, modelOption = null, apiKeyOption = null)
            config.model shouldBe "gemini-2.0-flash"
            config.provider shouldBe Provider.Google
        }

        test("when no specific configs are provided, default configs should be used") {
            val envConfigDataSource = FakeConfigDataSource(configs = EnvConfigs())
            val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs())
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = null, modelOption = null, apiKeyOption = null)
            config.model shouldBe "gpt-4o"
            config.provider shouldBe Provider.OpenAI
        }
    }

    context("language resolution") {
        test("when no target language option is provided, native and second languages should come from configs") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(nativeLanguage = "Japanese", secondLanguage = "English")
            )
            val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs())
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = null, modelOption = null, apiKeyOption = null)

            config.nativeLanguage shouldBe "Japanese"
            config.secondLanguage shouldBe "English"
        }

        test("when no languages are configured, defaults should be used") {
            val envConfigDataSource = FakeConfigDataSource(configs = EnvConfigs())
            val localConfigDataSource = FakeConfigDataSource(configs = LocalConfigs())
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = null, modelOption = null, apiKeyOption = null)

            config.nativeLanguage shouldBe "English"
            config.secondLanguage shouldBe "English"
        }

        test("local config should take precedence over env config") {
            val envConfigDataSource = FakeConfigDataSource(
                configs = EnvConfigs(nativeLanguage = "Japanese", secondLanguage = "English")
            )
            val localConfigDataSource = FakeConfigDataSource(
                configs = LocalConfigs(nativeLanguage = "Korean", secondLanguage = "Chinese")
            )
            val useCase = ResolveTranslatorConfigUseCase(
                envConfigDataSource = envConfigDataSource,
                localConfigDataSource = localConfigDataSource
            )

            val config = useCase(providerOption = null, modelOption = null, apiKeyOption = null)

            config.nativeLanguage shouldBe "Korean"
            config.secondLanguage shouldBe "Chinese"
        }
    }
})
