package me.orange.bot.events.interactions

import me.orange.bot.events.base.ButtonInteraction
import me.orange.game.GamesManager
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class InputInteraction(input: String) : ButtonInteraction(
    id = input,
    execute = { hook, event ->
        val buttonEvent = event as ButtonInteractionEvent
        val game = GamesManager.getGame(buttonEvent.guild!!.id)
        game.handleInput(hook, input)
    }
)