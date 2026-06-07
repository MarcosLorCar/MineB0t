package me.orange.bot.events.commands

import me.orange.bot.events.base.SlashCommand
import me.orange.game.GamesManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object PlayCommand : SlashCommand(
    id = "play",
    description = "Plays the game",
    execute = { hook, event ->
        val slashEvent = event as SlashCommandInteractionEvent
        val game = GamesManager.getGame(slashEvent.guild!!.id)
        game.updateHook(hook, showWorld = true)
    }
)