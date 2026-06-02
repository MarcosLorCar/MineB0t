package me.orange.bot.console.commands

import me.orange.bot.console.ConsoleCommand
import me.orange.game.GamesManager

object StatusCommand : ConsoleCommand(name = "status", description = "Show the number of active games") {
    override fun execute(args: List<String>) {
        println("Active games: ${GamesManager.games.size}")
    }
}
