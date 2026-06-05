package me.orange.bot.events.interactions

import me.orange.bot.MineB0t
import me.orange.game.GamesManager
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object HotbarInteraction : ListenerAdapter() {
    override fun onGenericEvent(event: GenericEvent) {
        if (event !is ButtonInteractionEvent) return
        val buttonId = event.button.id ?: return
        if (!buttonId.startsWith("hotbar_")) return

        event.deferEdit().queue { hook ->
            MineB0t.launch {
                val game = GamesManager.getGame(event.guild!!.id)
                game.handleInput(hook, buttonId)
            }
        }
    }
}
