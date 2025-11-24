package me.orange.events.commands

import me.orange.events.base.SlashCommand
import me.orange.game.GamesManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object PlayCommand : SlashCommand(
    id = "play",
    description = "Plays the game",
    execute = { hook, event ->
        val game = GamesManager.getGame((event as SlashCommandInteractionEvent).guild!!.id)
        game.updateHook(hook, showWorld = true)
    }
)