package me.orange.events.base

import me.orange.GamesManager
import me.orange.game.Game
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

abstract class ButtonInteraction(
    val id: String,
) : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val game = GamesManager.getGame(event.guild?.id ?: return)

        if (event.button.id == id) execute(event, game)
    }

    abstract fun execute(event: ButtonInteractionEvent, game: Game)
}