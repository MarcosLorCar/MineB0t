package me.orange.game.world.tile

import me.orange.game.craft.recipe.CraftingStationType
import me.orange.game.inventory.item.Item
import me.orange.game.inventory.item.ItemStack
import me.orange.game.utils.Vec
import me.orange.game.world.World
import net.dv8tion.jda.api.entities.emoji.Emoji

class Tile(
    val key: String,
    val emojiVariants: List<String>,
    val id: Int,
    val airy: Boolean,
    val breakable: Boolean,
    val drop: ItemStack?,
    val interaction: TileInteraction = TileInteraction.BareHanded,
    val onBreak: (World, Vec) -> Unit = { world, pos -> world.setTile(pos, Tiles.AIR) },
) {
    val emoji: String get() = emojiVariants[0]

    fun getEmoji(pos: Vec): String {
        if (emojiVariants.size == 1) return emojiVariants[0]
        var h = pos.x * 1619 xor pos.y * 31337
        h = h xor (h ushr 16)
        h *= -2048144789
        h = h xor (h ushr 16)
        return emojiVariants[(h ushr 1) % emojiVariants.size]
    }

    class Builder(val key: String, primaryEmoji: Emoji) {
        private val weightedVariants: MutableList<Pair<Emoji, Int>> = mutableListOf(primaryEmoji to 1)
        var airy: Boolean = false
        var breakable: Boolean = false
        var onBreak: (World, Vec) -> Unit = { world, pos -> world.setTile(pos, Tiles.AIR) }
        var drop: ItemStack? = null
        var interaction: TileInteraction = TileInteraction.BareHanded

        fun variant(emoji: Emoji, weight: Int = 1) = apply {
            val i = weightedVariants.indexOfFirst { it.first == emoji }
            if (i >= 0) weightedVariants[i] = emoji to weight else weightedVariants.add(emoji to weight)
        }
        fun airy() = apply { airy = true }
        fun breakable() = apply { breakable = true }
        fun onBreak(onBreak: (World, Vec) -> Unit) = apply { this.onBreak = onBreak }
        fun drops(item: Item, count: Int = 1) = apply { this.drop = ItemStack(item, count) }
        fun interaction(interaction: TileInteraction) = apply { this.interaction = interaction }
        fun craftingStation(type: CraftingStationType) = interaction(TileInteraction.CraftingStation(type))

        fun build(id: Int): Tile = Tile(
            key = key,
            emojiVariants = weightedVariants.flatMap { (emoji, weight) -> List(weight) { emoji.formatted } },
            id = id,
            airy = airy,
            breakable = breakable,
            drop = drop,
            interaction = interaction,
            onBreak = onBreak,
        )
    }
}
