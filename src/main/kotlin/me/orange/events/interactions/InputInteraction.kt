package me.orange.events.interactions

import me.orange.bot.MineB0t
import me.orange.events.base.ButtonInteraction
import me.orange.game.GamesManager
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class InputInteraction(val input: String) : ButtonInteraction(input) {
    override fun execute(
        event: ButtonInteractionEvent
    ) {
        event.deferEdit().queue {
            val game = GamesManager.getGame(event.guild?.id ?: return@queue)
            MineB0t.launch {
                game.handleInput(it, input)
                game.updateHook(it, true)
            }
        }
    }
}