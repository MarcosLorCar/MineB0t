package me.orange.events.interactions

import me.orange.events.interactions.base.ButtonInteraction
import me.orange.game.Game
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ChangeModeInteraction(val mode: String) : ButtonInteraction("changeMode_$mode") {
    override fun execute(event: ButtonInteractionEvent) {
        event.deferEdit().queue {
            Game.handleInput(it, "changeMode_$mode")
            Game.updateHook(it)
        }
    }
}