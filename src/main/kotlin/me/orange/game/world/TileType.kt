package me.orange.game.world

import kotlinx.serialization.Serializable
import me.orange.bot.Emojis
import me.orange.game.inventory.ItemType

@Serializable
enum class TileType(
    val emoji: String,
    val airy: Boolean = false,
    val breakable: Boolean = false,
    val item: ItemType? = null,
) {
    AIR(Emojis.getCustom("air").formatted, airy = true),
    GRASS(Emojis.getCustom("grass").formatted, breakable = true, item = ItemType.GRASS),
    DIRT(Emojis.getCustom("dirt").formatted, breakable = true, item = ItemType.DIRT),
    STONE(Emojis.getCustom("stone").formatted, breakable = true, item = ItemType.STONE),
    IRON_ORE(Emojis.getCustom("iron_ore").formatted, breakable = true, item = ItemType.IRON_CHUNK),
    NULL("❌"),
}