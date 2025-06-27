package org.example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int

class Hello : CliktCommand() {
    private val count: Int by option(help = "Number of greetings").int().default(1)
    private val name: String by option(help = "The name to greet").default("World")

    override fun run() {
        for (i in 1..count) {
            echo("Hello $name!")
        }
    }
}

fun main(args: Array<String>) = Hello().main(args)
