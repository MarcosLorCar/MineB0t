package me.orange.events.interactions

import kotlinx.coroutines.launch
import me.orange.game.GamesManager
import me.orange.events.base.ButtonInteraction
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class InputInteraction(val input: String) : ButtonInteraction(input) {
    override fun execute(
        event: ButtonInteractionEvent
    ) {
        event.deferEdit().queue {
            val game = GamesManager.getGame(event.guild?.id ?: return@queue)
            game.scope.launch {
                game.handleInput(it, input)
                game.updateHook(it, true)
            }
        }
    }
}