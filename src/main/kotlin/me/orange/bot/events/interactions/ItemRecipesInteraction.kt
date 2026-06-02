package me.orange.bot.events.interactions

import me.orange.bot.Emojis
import me.orange.bot.events.base.BaseInteraction
import me.orange.game.GamesManager
import me.orange.game.craft.CraftingStationType
import me.orange.game.craft.RecipeRegistry
import me.orange.game.inventory.ItemStack
import me.orange.game.player.Player
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

object ItemRecipesInteraction : BaseInteraction(
    id = "itemRecipes",
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
            val recipes = RecipeRegistry.getRecipesByIngredient(item)
            if (recipes.isEmpty()) {
                "**${item.displayName()}** is not used in any recipes."
            } else {
                val lines = recipes.joinToString("\n") { recipe ->
                    val inputs = recipe.ingredients.joinToString(" + ") { stackStr(it) }
                    val result = stackStr(recipe.result)
                    val station = if (recipe.requiredStation != CraftingStationType.NONE)
                        " (${Emojis.get(recipe.requiredStation.emojiKey).formatted})"
                    else ""
                    "$inputs ➔ $result$station"
                }
                "**Recipes using ${item.displayName()}:**\n$lines"
            }
        }
        hook.editOriginal(msg).queue()
    }
)

private fun stackStr(stack: ItemStack) = "**${stack.count}x** ${stack.item.emoji.formatted}"

private fun me.orange.game.inventory.Item.displayName() =
    key.split("_").joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
