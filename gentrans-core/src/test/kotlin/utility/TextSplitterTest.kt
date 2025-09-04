package utility

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TextSplitterTest : FunSpec({

    context("splitTextByLinesWithinSize") {
        test("should return empty list for empty string") {
            val result = splitTextByLinesWithinSize("", 100)
            result shouldBe emptyList()
        }

        test("should return single chunk for text smaller than maxSize") {
            val text = "Hello world"
            val result = splitTextByLinesWithinSize(text, 100)
            result shouldBe listOf("Hello world")
        }

        test("should return single chunk for single line text within limit") {
            val text = "This is a simple English sentence."
            val result = splitTextByLinesWithinSize(text, 50)
            result shouldBe listOf("This is a simple English sentence.")
        }

        test("should split multiple lines when combined size exceeds maxSize") {
            val text = "Line one is here.\nLine two is here.\nLine three is here."
            val result = splitTextByLinesWithinSize(text, 25)
            result shouldBe listOf("Line one is here.", "Line two is here.", "Line three is here.")
        }

        test("should combine lines when total size is within maxSize") {
            val text = "Short line.\nAnother short line.\nYet another."
            val result = splitTextByLinesWithinSize(text, 50)
            result shouldBe listOf("Short line.\nAnother short line.\nYet another.")
        }

        test("should handle text with only newlines") {
            val text = "\n\n\n"
            val result = splitTextByLinesWithinSize(text, 10)
            result shouldBe listOf("\n\n\n")
        }

        test("should handle mixed content with empty lines") {
            val text = "First line.\n\nThird line after empty."
            val result = splitTextByLinesWithinSize(text, 20)
            result shouldBe listOf("First line.\n", "Third line after empty.")
        }

        test("should split when first line exceeds maxSize") {
            val text = "This is a very long first line that definitely exceeds the maximum size limit.\nShort second line."
            val result = splitTextByLinesWithinSize(text, 30)
            result shouldBe listOf("This is a very long first line that definitely exceeds the maximum size limit.", "Short second line.")
        }

        test("should handle text ending with newline") {
            val text = "Line one.\nLine two.\n"
            val result = splitTextByLinesWithinSize(text, 15)
            result shouldBe listOf("Line one.", "Line two.\n")
        }

        test("should handle complex multiline text with varying line lengths") {
            val text = """First line is short.
Second line is much longer and contains more words.
Third.
Fourth line is also quite lengthy."""
            val result = splitTextByLinesWithinSize(text, 60)
            result shouldBe listOf(
                "First line is short.",
                "Second line is much longer and contains more words.\nThird.",
                "Fourth line is also quite lengthy."
            )
        }

        test("should handle text with multiple consecutive empty lines in the middle") {
            val text = "Beginning text.\n\n\n\nMiddle text after multiple blank lines.\nEnd text."
            val result = splitTextByLinesWithinSize(text, 40)
            result shouldBe listOf(
                "Beginning text.\n\n\n",
                "Middle text after multiple blank lines.",
                "End text."
            )
        }
    }
})
