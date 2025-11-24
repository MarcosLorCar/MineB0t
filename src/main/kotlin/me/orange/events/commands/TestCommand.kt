package me.orange.events.commands

import me.orange.events.base.SlashCommand
import me.orange.game.GamesManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object TestCommand : SlashCommand(
    id = "test",
    description = "This is a test command",
    execute = { hook, event ->
        val game = GamesManager.getGame((event as SlashCommandInteractionEvent).guild!!.id)
        game.preferencesManager.showMenu(hook)
    }
)