package me.orange.events.interactions

import me.orange.events.interactions.base.ButtonInteraction
import me.orange.game.Game
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ActionInteraction(val action: String) : ButtonInteraction("action_$action") {
    override fun execute(event: ButtonInteractionEvent) {
        event.deferEdit().queue {
            Game.handleInput(it, "action_$action")
            Game.updateHook(it)
        }
    }
}