package me.orange.events.interactions

import kotlinx.coroutines.launch
import me.orange.events.base.ButtonInteraction
import me.orange.game.Game
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class InputInteraction(val input: String) : ButtonInteraction(input) {
    override fun execute(
        event: ButtonInteractionEvent,
        game: Game
    ) {
        event.deferEdit().queue {
            game.scope.launch {
                game.handleInput(it, input)
                game.updateHook(it, true)
            }
        }
    }
}