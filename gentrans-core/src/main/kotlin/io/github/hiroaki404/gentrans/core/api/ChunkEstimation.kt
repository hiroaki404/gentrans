package io.github.hiroaki404.gentrans.core.api

import io.github.hiroaki404.gentrans.core.utility.splitForTranslation

public fun estimateChunkCount(text: String, format: InputFormat): Int =
    splitForTranslation(text, format).inputTexts.size
