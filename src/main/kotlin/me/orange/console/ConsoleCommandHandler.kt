package me.orange.console

import me.orange.console.commands.SaveCommand
import me.orange.console.commands.StatusCommand
import me.orange.console.commands.StopCommand

object ConsoleCommandHandler {
    private val commands: List<ConsoleCommand> = listOf(
        StopCommand,
        SaveCommand,
        StatusCommand,
    )

    fun dispatch(input: String) {
        val tokens = input.trim().split("\\s+".toRegex())
        val name = tokens.first()
        val args = tokens.drop(1)

        if (name == "help") {
            println("Available commands:")
            commands.forEach { println("  ${it.name} - ${it.description}") }
            println("  help - Show this help message")
            return
        }

        val command = commands.find { it.name == name }
        if (command == null) {
            println("Unknown command '$name'. Type 'help' for a list of commands.")
            return
        }

        command.execute(args)
    }
}
