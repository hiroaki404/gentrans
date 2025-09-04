package utility

fun splitTextByLinesWithinSize(text: String, maxSize: Int): List<String> {
    if (text.isEmpty()) return emptyList()
    if (text.length <= maxSize) return listOf(text)

    return text.split("\n")
        .fold(mutableListOf<String>() to "") { (result, currentChunk), line ->
            val testChunk = if (currentChunk.isEmpty()) line else "$currentChunk\n$line"

            when {
                testChunk.length <= maxSize -> result to testChunk
                else -> {
                    if (currentChunk.isNotEmpty()) result.add(currentChunk)
                    result to line
                }
            }
        }
        .let { (result, lastChunk) ->
            if (lastChunk.isNotEmpty()) result.add(lastChunk)
            result
        }
}
