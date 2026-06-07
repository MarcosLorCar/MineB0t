package me.orange.game.craft

import me.orange.bot.Emojis
import me.orange.game.craft.recipe.CraftingStationType
import me.orange.game.inventory.item.ItemStack
import me.orange.game.player.Player
import me.orange.game.preferences.Preference
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
        val categories = player.recipeManager.getViewableRecipes()
        if (categories.isEmpty()) return null

        val all = categories.all
        val craftableIds = categories.craftable.map { it.id }.toSet()
        val semiIds = categories.semi.map { it.id }.toSet()

        val perPage = player.game.preferencesManager.getPreference<Int>(player.id, Preference.MENUS_SIZE)
        val pageCount = (all.size + perPage - 1) / perPage
        val page = player.recipeManager.craftPage.coerceIn(0, pageCount - 1)
        player.recipeManager.craftPage = page

        val station = player.game.world.getCraftingStationAt(player.pos)
        val pageRecipes = all.subList(page * perPage, minOf(all.size, (page + 1) * perPage))

        val descriptionBuilder = StringBuilder()
        var lastCategory: String? = null

        pageRecipes.forEach { recipe ->
            val category = when (recipe.id) {
                in craftableIds -> "Craftable"
                in semiIds -> "Ingredients Owned"
                else -> "Known"
            }

            if (category != lastCategory) {
                if (lastCategory != null) descriptionBuilder.append("\n")
                descriptionBuilder.append("**$category**\n")
                lastCategory = category
            }

            val id = "[${recipe.id.replace("_", "\\_")}]"
            val result = stackString(recipe.result)
            val stationStr = if (recipe.requiredStation != CraftingStationType.NONE)
                "${recipe.requiredStation.emoji.formatted} " else ""
            
            val line = when (recipe.id) {
                in craftableIds -> {
                    val inputs = recipe.ingredients.joinToString(" + ") { stackString(it) }
                    "$id $stationStr$inputs ➔ $result"
                }
                in semiIds -> {
                    val inputs = recipe.ingredients.joinToString(" + ") { semiStackString(it) }
                    "$id $stationStr◆ $inputs ➔ $result"
                }
                else -> {
                    val inputs = recipe.ingredients.joinToString(" + ") { stackString(it) }
                    "~~$id ${stationStr}$inputs ➔ $result~~"
                }
            }
            descriptionBuilder.append("$line\n")
        }
        val description = descriptionBuilder.toString().trim()

        val feedback = player.feedback
        player.feedback = null

        val fullDescription = if (feedback != null) "$feedback\n\n$description" else description

        val embed = EmbedBuilder()
            .setTitle("${station.emoji.formatted} Crafting — page ${page + 1}/$pageCount")
            .setDescription(fullDescription)
            .setColor(CRAFTING_COLOR)
            .build()

        val nav = ActionRow.of(
            Button.of(ButtonStyle.SECONDARY, "craft_prev", Emojis.get("left")).withDisabled(page == 0),
            Button.of(ButtonStyle.SECONDARY, "craft_close", Emojis.get("return")),
            Button.of(ButtonStyle.SECONDARY, "craft_next", Emojis.get("right")).withDisabled(page >= pageCount - 1),
        )

        val craftableOnPage = pageRecipes.filter { it.id in craftableIds }
        val components = mutableListOf<LayoutComponent>(nav)

        if (craftableOnPage.isNotEmpty()) {
            val menu = StringSelectMenu.create("craft_select")
                .setPlaceholder("Craft an item")
                .addOptions(craftableOnPage.map { SelectOption.of("[${it.id}]", it.id) })
                .build()
            components.add(ActionRow.of(menu))
        }

        return embed to components
    }

    private fun stackString(stack: ItemStack): String = Emojis.renderStack(stack.item.emoji.formatted, stack.count)

    private fun semiStackString(stack: ItemStack): String {
        val have = player.inventory.countOf(stack.itemKey)
        return Emojis.renderSemiStack(stack.item.emoji.formatted, have, stack.count)
    }
}
