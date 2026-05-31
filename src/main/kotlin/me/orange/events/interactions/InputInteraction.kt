package me.orange.events.interactions

import me.orange.events.base.ButtonInteraction
import me.orange.game.GamesManager
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class InputInteraction(input: String) : ButtonInteraction(
    id = input,
    execute = { hook, event ->
        val game = GamesManager.getGame((event as ButtonInteractionEvent).guild!!.id)
        game.handleInput(hook, input)
    }
)