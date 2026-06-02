package me.orange.bot.console.commands

import me.orange.bot.console.ConsoleCommand
import kotlin.system.exitProcess

object StopCommand : ConsoleCommand(name = "stop", description = "Stop the bot") {
    override fun execute(args: List<String>) {
        println("Stopping bot...")
        exitProcess(0)
    }
}
