package me.orange.events.commands

import kotlinx.coroutines.launch
import me.orange.game.GamesManager
import me.orange.events.base.SlashCommand
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
                if (!event.isFromGuild) return@queue
                val game = GamesManager.getGame(event.guild!!.id)

                game.scope.launch {
                    game.showWorldToHook(it)
                }
            }
    }
}