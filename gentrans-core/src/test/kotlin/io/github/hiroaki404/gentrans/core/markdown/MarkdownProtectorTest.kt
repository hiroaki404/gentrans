package io.github.hiroaki404.gentrans.core.markdown

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MarkdownProtectorTest : FunSpec({

    context("protect") {
        test("should leave text unchanged when there is no front matter and no fenced code blocks") {
            val markdown = "# Title\n\nSome plain paragraph text.\n"
            val result = MarkdownProtector.protect(markdown)
            result.text shouldBe markdown
            result.blocks shouldBe emptyList()
        }

        test("should protect front matter only at the very start of the document") {
            val markdown = "---\ntitle: Hello\n---\n\n# Body\n"
            val result = MarkdownProtector.protect(markdown)
            result.text shouldBe "GENTRANS_BLOCK_0\n\n# Body\n"
            result.blocks shouldBe listOf("---\ntitle: Hello\n---")
        }

        test("should not treat a thematic break in the middle of the document as front matter") {
            val markdown = "# Title\n\n---\n\nMore text.\n"
            val result = MarkdownProtector.protect(markdown)
            result.text shouldBe markdown
            result.blocks shouldBe emptyList()
        }

        test("should protect a fenced code block using triple backticks with an info string") {
            val markdown = "Before.\n\n```kotlin\nval x = 1\n```\n\nAfter.\n"
            val result = MarkdownProtector.protect(markdown)
            result.text shouldBe "Before.\n\nGENTRANS_BLOCK_0\n\nAfter.\n"
            result.blocks shouldBe listOf("```kotlin\nval x = 1\n```")
        }

        test("should protect a fenced code block using tildes with an info string") {
            val markdown = "Before.\n\n~~~python\nprint(1)\n~~~\n\nAfter.\n"
            val result = MarkdownProtector.protect(markdown)
            result.text shouldBe "Before.\n\nGENTRANS_BLOCK_0\n\nAfter.\n"
            result.blocks shouldBe listOf("~~~python\nprint(1)\n~~~")
        }

        test("should protect a fenced code block without an info string") {
            val markdown = "```\nplain fence\n```\n"
            val result = MarkdownProtector.protect(markdown)
            result.text shouldBe "GENTRANS_BLOCK_0\n"
            result.blocks shouldBe listOf("```\nplain fence\n```")
        }

        test("should protect a fenced code block nested inside a list item") {
            val markdown = "- item one\n  ```kotlin\n  val x = 1\n  ```\n- item two\n"
            val result = MarkdownProtector.protect(markdown)
            result.blocks shouldBe listOf("```kotlin\n  val x = 1\n  ```")
            MarkdownProtector.restore(result.text, result.blocks) shouldBe markdown
        }

        test("should protect a fenced code block nested inside a blockquote") {
            val markdown = "> intro\n> ```kotlin\n> val x = 1\n> ```\n> outro\n"
            val result = MarkdownProtector.protect(markdown)
            result.blocks shouldBe listOf("```kotlin\n> val x = 1\n> ```")
            MarkdownProtector.restore(result.text, result.blocks) shouldBe markdown
        }

        test("should protect multiple fenced code blocks in document order") {
            val markdown = "```\nfirst\n```\n\nmiddle text\n\n```\nsecond\n```\n"
            val result = MarkdownProtector.protect(markdown)
            result.text shouldBe "GENTRANS_BLOCK_0\n\nmiddle text\n\nGENTRANS_BLOCK_1\n"
            result.blocks shouldBe listOf("```\nfirst\n```", "```\nsecond\n```")
        }

        test("should round-trip an unclosed fenced code block") {
            val markdown = "Before.\n\n```kotlin\nval x = 1\nno closing fence\n"
            val result = MarkdownProtector.protect(markdown)
            MarkdownProtector.restore(result.text, result.blocks) shouldBe markdown
        }

        test("should not protect inline code spans") {
            val markdown = "Use `val x = 1` inline.\n"
            val result = MarkdownProtector.protect(markdown)
            result.text shouldBe markdown
            result.blocks shouldBe emptyList()
        }

        test("should round-trip front matter combined with fenced code blocks") {
            val markdown = "---\ntitle: Doc\n---\n\nIntro.\n\n```\ncode here\n```\n\nEnd.\n"
            val result = MarkdownProtector.protect(markdown)
            MarkdownProtector.restore(result.text, result.blocks) shouldBe markdown
        }
    }

    context("restore") {
        test("should restore placeholders back to the original blocks") {
            val markdown = "Before.\n\n```kotlin\nval x = 1\n```\n\nAfter.\n"
            val protected = MarkdownProtector.protect(markdown)
            MarkdownProtector.restore(protected.text, protected.blocks) shouldBe markdown
        }

        test("should throw when a placeholder is missing") {
            val protected = MarkdownProtector.protect("```\ncode\n```\n")
            shouldThrow<PlaceholderMismatchException> {
                MarkdownProtector.restore("no placeholder here\n", protected.blocks)
            }
        }

        test("should throw when a placeholder appears more than once") {
            val protected = MarkdownProtector.protect("```\ncode\n```\n")
            shouldThrow<PlaceholderMismatchException> {
                MarkdownProtector.restore("GENTRANS_BLOCK_0 and GENTRANS_BLOCK_0 again\n", protected.blocks)
            }
        }

        test("should throw when a placeholder with an unknown index appears") {
            val protected = MarkdownProtector.protect("```\ncode\n```\n")
            shouldThrow<PlaceholderMismatchException> {
                MarkdownProtector.restore("GENTRANS_BLOCK_0 and GENTRANS_BLOCK_5\n", protected.blocks)
            }
        }

        test("should throw when a placeholder is altered with extra whitespace") {
            val protected = MarkdownProtector.protect("```\ncode\n```\n")
            shouldThrow<PlaceholderMismatchException> {
                MarkdownProtector.restore("GENTRANS_BLOCK_ 0\n", protected.blocks)
            }
        }

        test("should throw when a placeholder is altered to lowercase") {
            val protected = MarkdownProtector.protect("```\ncode\n```\n")
            shouldThrow<PlaceholderMismatchException> {
                MarkdownProtector.restore("gentrans_block_0\n", protected.blocks)
            }
        }

        test("should throw when a placeholder is altered with full-width characters") {
            val protected = MarkdownProtector.protect("```\ncode\n```\n")
            shouldThrow<PlaceholderMismatchException> {
                MarkdownProtector.restore("GENTRANS_BLOCK_０\n", protected.blocks)
            }
        }

        test("should throw when a placeholder is altered with dashes instead of underscores") {
            val protected = MarkdownProtector.protect("```\ncode\n```\n")
            shouldThrow<PlaceholderMismatchException> {
                MarkdownProtector.restore("GENTRANS-BLOCK-0\n", protected.blocks)
            }
        }

        test("should return the translated text unchanged when there are no blocks") {
            val translated = "Just plain translated text.\n"
            MarkdownProtector.restore(translated, emptyList()) shouldBe translated
        }
    }
})
