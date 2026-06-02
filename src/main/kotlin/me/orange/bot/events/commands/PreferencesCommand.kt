package me.orange.bot.events.commands

import me.orange.bot.events.base.SlashCommand
import me.orange.game.GamesManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object PreferencesCommand : SlashCommand(
    id = "settings",
    description = "Tweak user preferences",
    execute = { hook, event ->
        val game = GamesManager.getGame((event as SlashCommandInteractionEvent).guild!!.id)
        game.preferencesManager.showMenu(hook)
    }
)