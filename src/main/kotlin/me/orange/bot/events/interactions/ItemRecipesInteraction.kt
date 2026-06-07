package me.orange.bot.events.interactions

import me.orange.bot.Emojis
import me.orange.bot.events.base.ButtonInteraction
import me.orange.game.GamesManager
import me.orange.game.craft.recipe.CraftingStationType
import me.orange.game.craft.recipe.RecipeRegistry
import me.orange.game.inventory.item.ItemStack
import me.orange.game.player.Player
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

object ItemRecipesInteraction : ButtonInteraction(
    id = "itemRecipes",
    edit = false,
    execute = { hook, event ->
        val buttonEvent = event as ButtonInteractionEvent
        val game = GamesManager.getGame(buttonEvent.guild!!.id)
        val player = game.players[buttonEvent.user.idLong] as? Player
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
                    val stationStr = if (recipe.requiredStation != CraftingStationType.NONE)
                        "${recipe.requiredStation.emoji.formatted} " else ""
                    "$stationStr$inputs ➔ $result"
                }
                "**Recipes using ${item.displayName()}:**\n$lines"
            }
        }
        hook.editOriginal(msg).queue()
    }
)

private fun stackStr(stack: ItemStack) = Emojis.renderStack(stack.item.emoji.formatted, stack.count)

private fun me.orange.game.inventory.item.Item.displayName() =
    key.split("_").joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
