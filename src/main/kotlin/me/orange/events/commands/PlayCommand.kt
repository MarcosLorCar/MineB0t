package me.orange.events.commands

import me.orange.bot.MineB0t
import me.orange.events.base.SlashCommand
import me.orange.game.GamesManager
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

                MineB0t.launch {
                    game.updateHook(it, showWorld = true)
                }
            }
    }
}