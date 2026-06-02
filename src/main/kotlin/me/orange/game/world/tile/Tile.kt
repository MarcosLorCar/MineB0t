package me.orange.game.world.tile

import me.orange.bot.Emojis
import me.orange.game.craft.CraftingStationType
import me.orange.game.inventory.Item
import me.orange.game.inventory.ItemStack
import me.orange.game.utils.Vec
import kotlin.math.absoluteValue

class Tile(
    val key: String,
    val emojiVariants: List<String>,
    val id: Int,
    val airy: Boolean,
    val breakable: Boolean,
    val drop: ItemStack?,
    val craftingStationType: CraftingStationType = CraftingStationType.NONE,
    val onBreak: () -> Unit = {},
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

    class Builder(val key: String) {
        private val weightedVariants: MutableList<Pair<String, Int>> = mutableListOf(key to 1)
        var airy: Boolean = false
        var breakable: Boolean = false
        var onBreak: () -> Unit = {}
        var drop: ItemStack? = null
        var craftingStationType: CraftingStationType = CraftingStationType.NONE
        fun variant(variantKey: String, weight: Int = 1) = apply {
            val i = weightedVariants.indexOfFirst { it.first == variantKey }
            if (i >= 0) weightedVariants[i] = variantKey to weight else weightedVariants.add(variantKey to weight)
        }
        fun airy() = apply { airy = true }
        fun breakable() = apply { breakable = true }
        fun onBreak(onBreak: () -> Unit) = apply { this.onBreak = onBreak }
        fun drops(item: Item, count: Int = 1) = apply { this.drop = ItemStack(item, count) }
        fun craftingStation(type: CraftingStationType) = apply { this.craftingStationType = type }

        fun build(id: Int): Tile = Tile(
            key = key,
            emojiVariants = weightedVariants.flatMap { (variantKey, weight) -> List(weight) { Emojis.getFormatted(variantKey) } },
            id = id,
            airy = airy,
            breakable = breakable,
            drop = drop,
            craftingStationType = craftingStationType,
            onBreak = onBreak
        )
    }
}