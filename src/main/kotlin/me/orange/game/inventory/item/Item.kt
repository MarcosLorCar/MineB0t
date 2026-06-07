package me.orange.game.inventory.item

import me.orange.game.world.tile.Tile
import net.dv8tion.jda.api.entities.emoji.Emoji

class Item(
    val key: String,
    val emoji: Emoji,
    val maxCount: Int = 16,
    val tileFn: (() -> Tile)? = null,
    val description: String? = null,
    val onUse: () -> Unit = {},
) {
    fun getTile(): Tile? = tileFn?.invoke()

    class Builder(val key: String, val emoji: Emoji) {
        var maxCount: Int = 16
        var tileFn: (() -> Tile)? = null
        var description: String? = null
        var onUse: () -> Unit = {}

        fun placeable(tile: () -> Tile) = apply { this.tileFn = tile }
        fun maxCount(n: Int) = apply { maxCount = n }
        fun description(text: String) = apply { description = text }
        fun onUse(block: () -> Unit) = apply { onUse = block }
        fun build(): Item = Item(key, emoji, maxCount, tileFn, description, onUse)
    }
}
