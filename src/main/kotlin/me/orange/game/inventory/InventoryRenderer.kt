package me.orange.game.inventory

import me.orange.bot.Emojis
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

class InventoryRenderer(
    private val inventory: Inventory,
) {
    companion object {
        const val INVENTORY_COLOR = 11954716
    }

    fun getEmbed(): MessageEmbed? = with(inventory) {
        if (isEmpty()) return null

        val builder = EmbedBuilder()

        contents.forEachIndexed { index, stack ->
            val content = stack.count.toString().toCharArray().map { c -> Emojis.getNumber(c.digitToInt()) }.toMutableList()
            if (selectedSlot == index)
                content.add("\n${Emojis.getCustom("up").formatted}")
            builder.addField(
                stack.itemType.emoji.formatted,
                content.joinToString(""),
                true
            )
        }

        builder.setColor(INVENTORY_COLOR)

        return builder.build()
    }
}