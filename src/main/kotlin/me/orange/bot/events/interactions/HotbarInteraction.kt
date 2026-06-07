package me.orange.bot.events.interactions

import me.orange.bot.events.base.ButtonInteraction
import me.orange.bot.events.base.Interaction
import me.orange.game.GamesManager
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook

object HotbarInteraction : Interaction {
    override val id: String = "hotbar"
    override val edit: Boolean = true

    override fun matches(id: String): Boolean = id.startsWith("hotbar_")

    override val execute: suspend (InteractionHook, Any) -> Unit = { hook, event ->
        val buttonEvent = event as ButtonInteractionEvent
        val buttonId = buttonEvent.button.id!!
        val game = GamesManager.getGame(buttonEvent.guild!!.id)
        game.handleInput(hook, buttonId)
    }
}
