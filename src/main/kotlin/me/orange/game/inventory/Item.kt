package me.orange.game.inventory

import me.orange.bot.Emojis
import me.orange.game.world.tile.Tile
import me.orange.game.world.tile.TileRegistry
import net.dv8tion.jda.api.entities.emoji.Emoji

class Item(
    val key: String,
    val emoji: Emoji,
    val maxCount: Int = 16,
    val tileKey: String? = null,
    val description: String? = null,
    val onUse: () -> Unit = {},
) {
    fun getTile(): Tile? = tileKey?.let { TileRegistry.getTile(TileRegistry.getID(it)) }

    class Builder(val key: String) {
        var emoji: Emoji = if (Emojis.customEmoji.containsKey(key)) Emojis.getCustom(key) else Emojis.getEmoji(key)
        var maxCount: Int = 16
        var tileKey: String? = null
        var description: String? = null
        var onUse: () -> Unit = {}

        fun placeable(tileKey: String) = apply { this.tileKey = tileKey }
        fun maxCount(n: Int) = apply { maxCount = n }
        fun description(text: String) = apply { description = text }
        fun onUse(block: () -> Unit) = apply { onUse = block }
        fun build(): Item = Item(key, emoji, maxCount, tileKey, description, onUse)
    }
}
