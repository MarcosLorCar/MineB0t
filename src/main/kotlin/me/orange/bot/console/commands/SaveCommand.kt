package me.orange.bot.console.commands

import me.orange.bot.console.ConsoleCommand
import me.orange.game.GamesManager

object SaveCommand : ConsoleCommand(name = "save", description = "Save all active games to disk") {
    override fun execute(args: List<String>) {
        GamesManager.saveAll()
        println("Saved.")
    }
}
