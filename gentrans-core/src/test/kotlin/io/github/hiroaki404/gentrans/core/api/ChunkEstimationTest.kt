package io.github.hiroaki404.gentrans.core.api

import io.github.hiroaki404.gentrans.core.utility.splitForTranslation
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ChunkEstimationTest : FunSpec({
    test("estimateChunkCount matches plain text splitting") {
        val text = (1..1_100).joinToString("\n") { "line $it" }
        estimateChunkCount(text, InputFormat.PLAIN_TEXT) shouldBe splitForTranslation(text, InputFormat.PLAIN_TEXT).inputTexts.size
    }

    test("estimateChunkCount matches markdown splitting") {
        val text = (1..1_100).joinToString("\n\n") { "## Heading $it\n\ncontent" }
        estimateChunkCount(text, InputFormat.MARKDOWN) shouldBe splitForTranslation(text, InputFormat.MARKDOWN).inputTexts.size
    }
})
