import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MyFirstTest : StringSpec({
    "length should return the length of a string" {
        "hello".length shouldBe 5
    }
})
