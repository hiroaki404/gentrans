package io.github.hiroaki404.gentrans.core.markdown

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private const val GOLDEN_CHUNK_DELIMITER = "\n<<<CHUNK>>>\n"

private fun readResource(path: String): String {
    val stream = object {}.javaClass.classLoader.getResourceAsStream(path)
        ?: error("resource not found: $path")
    return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
}

private data class GoldenCase(val name: String, val maxSize: Int)

class MarkdownSegmenterTest : FunSpec({

    context("split") {
        test("should return an empty list for an empty string") {
            MarkdownSegmenter.split("", 100) shouldBe emptyList()
        }

        test("should return an empty list for whitespace-only input") {
            MarkdownSegmenter.split("   \n\n  \n", 100) shouldBe emptyList()
        }

        test("should return a single trimmed chunk when the document fits within maxSize") {
            val markdown = "  \nHello world.\n\n"
            MarkdownSegmenter.split(markdown, 100) shouldBe listOf("Hello world.")
        }

        test("should split at a block boundary when the next block would exceed maxSize") {
            val markdown = "First paragraph.\n\nSecond paragraph.\n\nThird paragraph.\n"
            val result = MarkdownSegmenter.split(markdown, 20)
            result shouldBe listOf("First paragraph.", "Second paragraph.", "Third paragraph.")
        }

        test("should combine consecutive blocks into one chunk while they fit within maxSize") {
            val markdown = "One.\n\nTwo.\n\nThree.\n"
            val result = MarkdownSegmenter.split(markdown, 100)
            result shouldBe listOf("One.\n\nTwo.\n\nThree.")
        }

        test("should keep a single block that alone exceeds maxSize as one unsplit chunk") {
            val markdown = "| a | b |\n|---|---|\n| 1 | 2 |\n| 3 | 4 |\n"
            val result = MarkdownSegmenter.split(markdown, 5)
            result shouldBe listOf("| a | b |\n|---|---|\n| 1 | 2 |\n| 3 | 4 |")
        }

        test("should isolate an oversized block from smaller neighboring blocks") {
            val markdown = "Short.\n\n| a | b |\n|---|---|\n| 1 | 2 |\n\nShort again.\n"
            val result = MarkdownSegmenter.split(markdown, 15)
            result shouldBe listOf("Short.", "| a | b |\n|---|---|\n| 1 | 2 |", "Short again.")
        }

        test("should keep a heading and an immediately following paragraph with no blank line together when they fit") {
            val markdown = "# Title\nBody right after heading.\n"
            val result = MarkdownSegmenter.split(markdown, 100)
            result shouldBe listOf("# Title\nBody right after heading.")
        }

        test("should split a heading from an immediately following paragraph when they do not fit together") {
            val markdown = "# Title\nBody right after heading.\n"
            val result = MarkdownSegmenter.split(markdown, 10)
            result shouldBe listOf("# Title", "Body right after heading.")
        }

        test("should keep a nested list as a single block") {
            val markdown = "- item a\n  - nested item\n- item b\n"
            val result = MarkdownSegmenter.split(markdown, 100)
            result shouldBe listOf("- item a\n  - nested item\n- item b")
        }

        test("should keep a nested blockquote as a single block") {
            val markdown = "> outer\n> > inner\n> outer again\n"
            val result = MarkdownSegmenter.split(markdown, 100)
            result shouldBe listOf("> outer\n> > inner\n> outer again")
        }

        test("should treat a fenced code placeholder line like any other top-level block") {
            val markdown = "Intro.\n\nGENTRANS_BLOCK_0\n\nOutro.\n"
            val result = MarkdownSegmenter.split(markdown, 100)
            result shouldBe listOf("Intro.\n\nGENTRANS_BLOCK_0\n\nOutro.")
        }
    }

    context("golden") {
        val cases = listOf(
            GoldenCase("heading-and-paragraphs", 40),
            GoldenCase("lists", 40),
            GoldenCase("table", 60),
            GoldenCase("blockquote", 40),
            GoldenCase("placeholders", 30),
            GoldenCase("html-block", 40),
            GoldenCase("horizontal-rule", 30),
            GoldenCase("oversized-block", 20),
        )

        cases.forEach { case ->
            test("should match the golden output for '${case.name}'") {
                val markdown = readResource("markdown/golden/${case.name}.md")
                val expected = readResource("markdown/golden/${case.name}.expected.txt")
                    .split(GOLDEN_CHUNK_DELIMITER)
                val result = MarkdownSegmenter.split(markdown, case.maxSize)
                result shouldBe expected
            }
        }

        test("should match the golden output for the protected README") {
            val markdown = readResource("markdown/golden/readme-protected.md")
            val expected = readResource("markdown/golden/readme-protected.expected.txt")
                .split(GOLDEN_CHUNK_DELIMITER)
            val result = MarkdownSegmenter.split(markdown, 2_000)
            result shouldBe expected
        }
    }

    context("properties") {
        val fixtureNames = listOf(
            "heading-and-paragraphs",
            "lists",
            "table",
            "blockquote",
            "placeholders",
            "html-block",
            "horizontal-rule",
            "oversized-block",
        )
        val maxSizes = mapOf(
            "heading-and-paragraphs" to 40,
            "lists" to 40,
            "table" to 60,
            "blockquote" to 40,
            "placeholders" to 30,
            "html-block" to 40,
            "horizontal-rule" to 30,
            "oversized-block" to 20,
        )

        (fixtureNames + "readme-protected").forEach { name ->
            test("chunks for '$name' are non-overlapping substrings with only whitespace between them") {
                val markdown = readResource("markdown/golden/$name.md")
                val maxSize = maxSizes[name] ?: 2_000
                val chunks = MarkdownSegmenter.split(markdown, maxSize)

                var cursor = 0
                for (chunk in chunks) {
                    val foundAt = markdown.indexOf(chunk, cursor)
                    foundAt shouldBe (foundAt.takeIf { it >= cursor } ?: -1)
                    (foundAt >= cursor) shouldBe true

                    val gap = markdown.substring(cursor, foundAt)
                    gap.isBlank() shouldBe true

                    cursor = foundAt + chunk.length
                }
            }
        }
    }
})
