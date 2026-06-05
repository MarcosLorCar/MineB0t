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

        val air = Emojis.getFormatted("air")
        val selectedIndicator = Emojis.getFormatted("left")
        val builder = EmbedBuilder()

        contents.forEachIndexed { index, stack ->
            val indicator = if (selectedSlot == index) selectedIndicator else air
            val digits = stack.count.toString().map { c -> Emojis.getNumber(c.digitToInt()) }
            val countRow = if (digits.size == 1) "${digits[0]}$air" else digits.joinToString("")

            builder.addField("${stack.item.emoji.formatted}$indicator", countRow, true)
        }

        val remainder = contents.size % INVENTORY_COLS
        if (remainder != 0) {
            repeat(INVENTORY_COLS - remainder) {
                builder.addField("$air$air", "$air$air", true)
            }
        }

        builder.setColor(INVENTORY_COLOR)

        return builder.build()
    }
}
