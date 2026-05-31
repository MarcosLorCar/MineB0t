package me.orange.game.craft

import me.orange.bot.Config
import me.orange.bot.Emojis
import me.orange.game.inventory.ItemStack
import me.orange.game.player.Player
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

class CraftingRenderer(
    private val player: Player,
) {
    companion object {
        const val CRAFTING_COLOR = 0xB5651D
    }

    /** Embed + components for the current crafting page, or null if there are no viewable recipes. */
    fun render(): Pair<MessageEmbed, List<LayoutComponent>>? {
        val recipes = player.recipeManager.getViewableRecipes()
        if (recipes.isEmpty()) return null

        val perPage = Config.RECIPES_PER_PAGE
        val pageCount = (recipes.size + perPage - 1) / perPage
        val page = player.recipeManager.craftPage.coerceIn(0, pageCount - 1)
        player.recipeManager.craftPage = page

        val station = player.game.world.getCraftingStationAt(player.pos)
        val pageRecipes = recipes.subList(page * perPage, minOf(recipes.size, (page + 1) * perPage))

        val description = pageRecipes.joinToString("\n") { recipe ->
            val inputs = recipe.ingredients.joinToString(" + ") { stackString(it) }
            "[${recipe.id}] $inputs ➔ ${stackString(recipe.result)}"
        }

        val embed = EmbedBuilder()
            .setTitle("${Emojis.get(station.emojiKey).formatted} Crafting — page ${page + 1}/$pageCount")
            .setDescription(description)
            .setColor(CRAFTING_COLOR)
            .build()

        val nav = ActionRow.of(
            Button.of(ButtonStyle.SECONDARY, "craft_prev", Emojis.getCustom("left")).withDisabled(page == 0),
            Button.of(ButtonStyle.SECONDARY, "craft_close", Emojis.getEmoji("return")),
            Button.of(ButtonStyle.SECONDARY, "craft_next", Emojis.getCustom("right")).withDisabled(page >= pageCount - 1),
        )

        val menu = StringSelectMenu.create("craft_select")
            .setPlaceholder("Craft an item")
            .addOptions(pageRecipes.map { SelectOption.of("[${it.id}]", it.id) })
            .build()

        return embed to listOf(nav, ActionRow.of(menu))
    }

    private fun stackString(stack: ItemStack): String = "**${stack.count}x** ${stack.item.emoji.formatted}"
}
