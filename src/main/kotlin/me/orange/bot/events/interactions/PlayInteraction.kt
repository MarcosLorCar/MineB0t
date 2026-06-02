package me.orange.bot.events.interactions

import me.orange.bot.events.base.ButtonInteraction
import me.orange.game.GamesManager
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

object PlayInteraction : ButtonInteraction(
    id = "play",
    execute = { hook, event ->
        val game = GamesManager.getGame((event as ButtonInteractionEvent).guild!!.id)
        game.updateHook(hook, true)
    }
)