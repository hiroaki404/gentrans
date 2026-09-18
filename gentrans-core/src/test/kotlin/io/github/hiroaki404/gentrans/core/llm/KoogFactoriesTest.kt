package io.github.hiroaki404.gentrans.core.llm

import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class KoogFactoriesTest : FunSpec({

    context("buildLLMClient") {
        test("google with an api key returns a GoogleLLMClient") {
            val client = buildLLMClient("google", "google-key")
            client.shouldBeInstanceOf<GoogleLLMClient>()
        }

        test("openai with an api key returns an OpenAILLMClient") {
            val client = buildLLMClient("openai", "openai-key")
            client.shouldBeInstanceOf<OpenAILLMClient>()
        }

        test("anthropic with an api key returns an AnthropicLLMClient") {
            val client = buildLLMClient("anthropic", "anthropic-key")
            client.shouldBeInstanceOf<AnthropicLLMClient>()
        }

        test("google without an api key throws IllegalArgumentException") {
            shouldThrow<IllegalArgumentException> {
                buildLLMClient("google", null)
            }.message shouldBe "Google API key is required"
        }

        test("openai without an api key throws IllegalArgumentException") {
            shouldThrow<IllegalArgumentException> {
                buildLLMClient("openai", null)
            }.message shouldBe "Openai API key is required"
        }

        test("anthropic without an api key throws IllegalArgumentException") {
            shouldThrow<IllegalArgumentException> {
                buildLLMClient("anthropic", null)
            }.message shouldBe "Anthropic API key is required"
        }

        test("ollama without an api key returns an OllamaClient") {
            val client = buildLLMClient("ollama", null)
            client.shouldBeInstanceOf<OllamaClient>()
        }

        test("unknown provider throws IllegalArgumentException") {
            shouldThrow<IllegalArgumentException> {
                buildLLMClient("unknown-provider", "some-key")
            }.message shouldBe "Unknown provider: unknown-provider"
        }
    }

    context("buildLLModel") {
        test("adds OpenAIEndpoint.Completions capability for openai and openrouter providers only") {
            buildLLModel("gpt-4o", "openai").capabilities.orEmpty() shouldContain LLMCapability.OpenAIEndpoint.Completions
            buildLLModel("some-model", "openrouter").capabilities.orEmpty() shouldContain LLMCapability.OpenAIEndpoint.Completions
            buildLLModel("claude-sonnet-4-0", "anthropic").capabilities.orEmpty() shouldNotContain LLMCapability.OpenAIEndpoint.Completions
            buildLLModel("llama3.2", "ollama").capabilities.orEmpty() shouldNotContain LLMCapability.OpenAIEndpoint.Completions
        }
    }
})
