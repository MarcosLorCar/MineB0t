package me.orange.bot.console

abstract class ConsoleCommand(
    val name: String,
    val description: String,
) {
    abstract fun execute(args: List<String>)
}
