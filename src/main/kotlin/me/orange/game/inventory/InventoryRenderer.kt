package me.orange.game.inventory

import me.orange.bot.Emojis
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

class InventoryRenderer(
    private val inventory: Inventory,
) {
    companion object {
        const val INVENTORY_COLOR = 11954716
        const val INVENTORY_COLS = 3
    }

    fun getEmbed(): MessageEmbed? = with(inventory) {
        if (isEmpty()) return null

        val builder = EmbedBuilder()

        contents.forEachIndexed { index, stack ->
            val content = stack.count.toString().toCharArray().map { c -> Emojis.getNumber(c.digitToInt()) }.toMutableList()
            if (selectedSlot == index)
                content.add("\n${Emojis.getFormatted("up")}")
            builder.addField(
                stack.item.emoji.formatted,
                content.joinToString(""),
                true
            )
        }

        builder.setColor(INVENTORY_COLOR)

        return builder.build()
    }
}
