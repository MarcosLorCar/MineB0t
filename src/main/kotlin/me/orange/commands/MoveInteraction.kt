package me.orange.commands

import me.orange.commands.base.ButtonInteraction
import me.orange.game.Game
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class MoveInteraction(val direction: String) : ButtonInteraction("move_$direction") {
    override fun execute(event: ButtonInteractionEvent) {
        event.deferEdit().queue {
            Game.handleInput(it, "move_$direction")
            Game.updateHook(it)
        }
    }
}