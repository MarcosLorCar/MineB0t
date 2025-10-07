package me.orange.events.commands

import kotlinx.coroutines.launch
import me.orange.events.base.SlashCommand
import me.orange.game.GamesManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object PreferencesCommand : SlashCommand(
    name = "settings",
    description = "Tweak user preferences",
) {
    override fun execute(event: SlashCommandInteractionEvent) {
        event.deferReply()
            .setEphemeral(true)
            .queue {
                val game = GamesManager.getGame(event.guild?.id ?: return@queue)

                game.scope.launch {
                    game.preferencesManager.showMenu(it)
                }
            }
    }
}