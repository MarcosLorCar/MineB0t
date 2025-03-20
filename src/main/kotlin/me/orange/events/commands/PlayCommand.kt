package me.orange.events.commands

import me.orange.events.commands.base.SlashCommand
import me.orange.game.Game
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object PlayCommand : SlashCommand(
    name = "play",
    description = "Plays the game",
) {
    override fun execute(event: SlashCommandInteractionEvent) {
        // Hangs the response in case it takes a lot to load
        event.deferReply()
            .setEphemeral(true)
            .queue {
                Game.updateHook(it)
            }
    }
}