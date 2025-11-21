package me.orange.events.interactions

import me.orange.bot.MineB0t
import me.orange.events.base.ButtonInteraction
import me.orange.game.GamesManager
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

object PlayInteraction : ButtonInteraction("play") {
    override fun execute(event: ButtonInteractionEvent) {
        event.deferEdit()
            .queue {
                val game = GamesManager.getGame(event.guild?.id ?: return@queue)
                MineB0t.launch {
                    game.updateHook(it, true)
                }
            }
    }
}