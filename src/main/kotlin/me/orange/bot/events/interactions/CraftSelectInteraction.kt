package me.orange.bot.events.interactions

import me.orange.bot.events.base.StringSelectInteraction
import me.orange.game.GamesManager
import me.orange.game.player.Player
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

/**
 * Dropdown under the crafting view. Selecting a recipe id queues the craft (so inventory mutation
 * stays off the render path) and re-renders the crafting view on the next tick.
 */
object CraftSelectInteraction : StringSelectInteraction(
    id = "craft_select",
    execute = { hook, event ->
        val selectEvent = event as StringSelectInteractionEvent
        val game = GamesManager.getGame(selectEvent.guild!!.id)
        (game.players[selectEvent.user.idLong] as? Player)?.let { player ->
            val recipeId = selectEvent.selectedOptions.first().value
            game.refreshPlayer(player)
            player.hook = hook
            player.queueAction {
                it.recipeManager.craft(recipeId)
                it.game.playerCraftUiCache.remove(it.id)
            }
            player.queueAction { it.game.renderCrafting(it) }
        }
    }
)
