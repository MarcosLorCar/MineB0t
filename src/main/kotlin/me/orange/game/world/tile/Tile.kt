package me.orange.game.world.tile

import me.orange.bot.Emojis
import me.orange.game.inventory.Item
import me.orange.game.inventory.ItemStack

class Tile(
    val key: String,
    val emoji: String,
    val id: Int,
    val airy: Boolean,
    val breakable: Boolean,
    val drop: ItemStack?,
    val onBreak: () -> Unit = {},
) {
    class Builder(val key: String) {
        var emoji: String = if (Emojis.customEmoji.containsKey(key)) Emojis.getCustom(key).formatted else Emojis.getEmoji(key).formatted
        var airy: Boolean = false
        var breakable: Boolean = false
        var onBreak: () -> Unit = {}
        var drop: ItemStack? = null
        fun emoji(emoji: String) = apply { this.emoji = emoji }
        fun airy() = apply { airy = true }
        fun breakable() = apply { breakable = true }
        fun onBreak(onBreak: () -> Unit) = apply { this.onBreak = onBreak }
        fun drops(item: Item, count: Int = 1) = apply { this.drop = ItemStack(item, count) }

        fun build(id: Int) : Tile = Tile(
            key = key,
            emoji = emoji,
            id = id,
            airy = airy,
            breakable = breakable,
            drop = drop,
            onBreak = onBreak
        )
    }
}