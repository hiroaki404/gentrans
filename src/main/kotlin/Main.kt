package org.example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.versionOption
import org.example.gentrans.BuildConfig

class GenTransCommand : CliktCommand() {
    init {
        versionOption(BuildConfig.VERSION)
    }

    private val targetText: String? by argument(help = "Text to translate. Reads from stdin if not provided.").optional()

    override fun run() {
        val text = targetText ?: run {
            val stdinText = readlnOrNull()
            stdinText
        }
        echo("Translating: $text")
        // Here you would add the actual translation logic
    }
}

fun main(args: Array<String>) = GenTransCommand().main(args)
