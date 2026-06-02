package me.orange.console.commands

import me.orange.console.ConsoleCommand
import kotlin.system.exitProcess

object StopCommand : ConsoleCommand(name = "stop", description = "Stop the bot") {
    override fun execute(args: List<String>) {
        println("Stopping bot...")
        exitProcess(0)
    }
}
