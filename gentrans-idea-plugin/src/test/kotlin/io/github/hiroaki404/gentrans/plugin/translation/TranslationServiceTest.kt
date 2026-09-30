package io.github.hiroaki404.gentrans.plugin.translation

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.ModerationResult
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.ResponseMetaInfo
import ai.koog.prompt.streaming.StreamFrame
import ai.koog.utils.time.KoogClock
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.github.hiroaki404.gentrans.core.api.InternalGentransApi
import io.github.hiroaki404.gentrans.core.api.Provider
import io.github.hiroaki404.gentrans.core.api.TranslationEvent
import io.github.hiroaki404.gentrans.core.api.TranslatorConfig
import io.github.hiroaki404.gentrans.core.api.createTranslator
import io.github.hiroaki404.gentrans.plugin.settings.ApiKeyStore
import io.github.hiroaki404.gentrans.plugin.settings.GentransSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

internal class FakeApiKeyStore(private val key: String? = "dummy") : ApiKeyStore {
    override fun get(provider: Provider): String? = key

    override fun set(provider: Provider, apiKey: String?) = Unit
}

/**
 * A [PromptExecutor] test double, modeled on `TranslatorTest.IdentityPromptExecutor`: answers
 * language detection/decision with fixed languages and echoes the translate prompt's source text.
 * [gateAtCallIndex] optionally pauses the call at that 0-based index on [gate] after signalling
 * [entered], letting tests observe "the strategy graph has reached this LLM call" before it's
 * allowed to proceed - or never allowed to, if cancelled first.
 */
internal class ScriptedPromptExecutor(
    private val entered: CompletableDeferred<Unit>? = null,
    private val gate: CompletableDeferred<Unit>? = null,
    private val gateAtCallIndex: Int = -1,
    private val language: String = "English",
    private val translated: String = "Hello World!",
) : PromptExecutor() {
    private var callIndex = 0

    override suspend fun execute(prompt: Prompt, model: LLModel, tools: List<ToolDescriptor>): Message.Assistant {
        val index = callIndex++
        if (index == gateAtCallIndex) {
            entered?.complete(Unit)
            gate?.await()
        }
        val lastUserText = prompt.messages.filterIsInstance<Message.User>().last().textContent()
        val content = when {
            lastUserText.startsWith("Translate the following text from") -> translated
            index == 0 -> "Japanese"
            else -> language
        }
        return Message.Assistant(content, ResponseMetaInfo(timestamp = KoogClock.System.now()))
    }

    override fun executeStreaming(prompt: Prompt, model: LLModel, tools: List<ToolDescriptor>): Flow<StreamFrame> =
        throw UnsupportedOperationException("not used by these tests")

    override suspend fun moderate(prompt: Prompt, model: LLModel): ModerationResult =
        throw UnsupportedOperationException("not used by these tests")

    override fun close() = Unit
}

@OptIn(InternalGentransApi::class)
class TranslationServiceTest : BasePlatformTestCase() {
    // TranslationService bounces events through Dispatchers.EDT; running the test body itself on
    // the EDT (the BasePlatformTestCase default) would deadlock any blocking wait below.
    override fun runInDispatchThread(): Boolean = false

    private val config = TranslatorConfig(
        provider = Provider.OpenAI,
        model = "gpt-4o",
        apiKey = "dummy",
        nativeLanguage = "English",
        secondLanguage = "English",
    )

    private fun newScope() = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun testEventsArriveInOrder() {
        val scope = newScope()
        try {
            var usedConfig: TranslatorConfig? = null
            val service = TranslationService(
                project,
                scope,
                translatorFactory = {
                    usedConfig = it
                    createTranslator(config, executorFactory = { ScriptedPromptExecutor() })
                },
                apiKeyStore = FakeApiKeyStore(),
            )
            val events = mutableListOf<TranslationEvent>()
            val origins = mutableListOf<TranslationOrigin>()
            val completed = CompletableDeferred<Unit>()

            service.translate(
                "hello",
                onEvent = {
                    events += it
                    if (it is TranslationEvent.Completed) completed.complete(Unit)
                },
                onFailure = { fail("unexpected failure: $it") },
                onCompletedOrigin = { origins += it },
            )
            runBlocking { withTimeout(15_000) { completed.await() } }

            assertEquals(
                listOf(
                    TranslationEvent.SourceLanguageDetected(language = "Japanese", totalChunks = 1),
                    TranslationEvent.TargetLanguageDecided(language = "English"),
                    TranslationEvent.ChunkTranslated(
                        chunk = "Hello World!",
                        translatedTextSoFar = "Hello World!",
                        translatedChunks = 1,
                        totalChunks = 1,
                    ),
                    TranslationEvent.Completed(text = "Hello World!"),
                ),
                events,
            )
            assertEquals(listOf(TranslationOrigin(usedConfig!!.provider.key, usedConfig!!.model)), origins)
        } finally {
            scope.cancel()
        }
    }

    fun testStartingANewTranslationCancelsThePreviousJob() {
        val scope = newScope()
        try {
            val entered = CompletableDeferred<Unit>()
            val gate = CompletableDeferred<Unit>()
            var callCount = 0
            val service = TranslationService(
                project,
                scope,
                translatorFactory = {
                    val executor = if (callCount++ == 0) {
                        ScriptedPromptExecutor(entered = entered, gate = gate, gateAtCallIndex = 1)
                    } else {
                        ScriptedPromptExecutor()
                    }
                    createTranslator(config, executorFactory = { executor })
                },
                apiKeyStore = FakeApiKeyStore(),
            )

            val eventsA = mutableListOf<TranslationEvent>()
            val eventsB = mutableListOf<TranslationEvent>()
            val completedB = CompletableDeferred<Unit>()

            service.translate("first", onEvent = { eventsA += it }, onFailure = { fail("unexpected failure: $it") })
            runBlocking { withTimeout(15_000) { entered.await() } }

            service.translate(
                "second",
                onEvent = {
                    eventsB += it
                    if (it is TranslationEvent.Completed) completedB.complete(Unit)
                },
                onFailure = { fail("unexpected failure: $it") },
            )
            runBlocking { withTimeout(15_000) { completedB.await() } }
            gate.complete(Unit)

            assertEquals(listOf(TranslationEvent.SourceLanguageDetected(language = "Japanese", totalChunks = 1)), eventsA)
            assertTrue(eventsB.last() is TranslationEvent.Completed)
        } finally {
            scope.cancel()
        }
    }

    fun testStaleEventsAfterACancelAreDropped() {
        val scope = newScope()
        try {
            val entered = CompletableDeferred<Unit>()
            val gate = CompletableDeferred<Unit>()
            val service = TranslationService(
                project,
                scope,
                translatorFactory = {
                    createTranslator(
                        config,
                        executorFactory = { ScriptedPromptExecutor(entered = entered, gate = gate, gateAtCallIndex = 1) },
                    )
                },
                apiKeyStore = FakeApiKeyStore(),
            )

            val events = mutableListOf<TranslationEvent>()
            val origins = mutableListOf<TranslationOrigin>()
            service.translate(
                "hello",
                onEvent = { events += it },
                onFailure = { fail("unexpected failure: $it") },
                onCompletedOrigin = { origins += it },
            )
            runBlocking { withTimeout(15_000) { entered.await() } }

            service.cancel()
            gate.complete(Unit)
            // Give the cancelled coroutine a chance to (wrongly) deliver more events, if it were going to.
            runBlocking { delay(500) }

            assertEquals(listOf(TranslationEvent.SourceLanguageDetected(language = "Japanese", totalChunks = 1)), events)
            assertTrue(origins.isEmpty())
        } finally {
            scope.cancel()
        }
    }

    fun testAMissingApiKeyIsClassifiedAsApiKeyMissing() {
        // TranslationService resolves the provider from the real app-level settings service, not
        // from the [config] above (that's only used inside translatorFactory); pin it to OpenAI
        // regardless of what other tests left behind.
        val settings = GentransSettings.getInstance()
        val originalState = settings.getState()
        settings.loadState(GentransSettings.State(provider = Provider.OpenAI.key))
        val scope = newScope()
        try {
            val service = TranslationService(
                project,
                scope,
                translatorFactory = {
                    createTranslator(
                        config,
                        executorFactory = { throw IllegalArgumentException("OpenAI API key is required") },
                    )
                },
                apiKeyStore = FakeApiKeyStore(key = null),
            )

            val failure = CompletableDeferred<TranslationFailure>()
            service.translate(
                "hello",
                onEvent = { fail("unexpected event: $it") },
                onFailure = { failure.complete(it) },
            )
            val result = runBlocking { withTimeout(15_000) { failure.await() } }

            assertTrue(result is TranslationFailure.ApiKeyMissing)
            assertEquals(Provider.OpenAI, (result as TranslationFailure.ApiKeyMissing).provider)
        } finally {
            scope.cancel()
            settings.loadState(originalState)
        }
    }
}
