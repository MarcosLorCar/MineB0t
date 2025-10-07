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
        event.deferReply()
            .setEphemeral(true)
            .queue {
                val game = GamesManager.getGame(event.guild?.id ?: return@queue)

                game.scope.launch {
                    game.updateHook(it, showWorld = true)
                }
            }
    }
}