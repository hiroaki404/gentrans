package org.example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional

class GenTransCommand : CliktCommand() {
    private val targetText: String? by argument(help = "Text to translate. Reads from stdin if not provided.").optional()

    override fun run() {
        val text = targetText ?: run {
            val stdinText = generateSequence(::readlnOrNull).joinToString("\n")
            stdinText
        }
        echo("Translating: $text")
        // Here you would add the actual translation logic
    }
}

fun main(args: Array<String>) = GenTransCommand().main(args)
