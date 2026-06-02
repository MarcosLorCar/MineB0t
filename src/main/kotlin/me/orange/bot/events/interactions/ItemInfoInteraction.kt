package me.orange.bot.events.interactions

import me.orange.bot.events.base.BaseInteraction
import me.orange.game.GamesManager
import me.orange.game.player.Player
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

object ItemInfoInteraction : BaseInteraction(
    id = "itemInfo",
    getIdentifier = { (it as? ButtonInteractionEvent)?.button?.id },
    edit = false,
    execute = { hook, event ->
        event as ButtonInteractionEvent
        val game = GamesManager.getGame(event.guild!!.id)
        val player = game.players[event.user.idLong] as? Player
        val item = player?.inventory?.getSelectedItemStack()?.item

        val msg = if (item == null) {
            "No item selected."
        } else {
            val name = item.key.split("_").joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
            val emoji = item.emoji.formatted
            if (item.description != null) "$emoji - **$name**\n${item.description}" else "$emoji - **$name**"
        }
        hook.editOriginal(msg).queue()
    }
)
