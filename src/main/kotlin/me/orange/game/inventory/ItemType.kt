package me.orange.game.inventory

import kotlinx.serialization.Serializable
import me.orange.bot.Emojis
import me.orange.game.world.tile.Tile
import me.orange.game.world.tile.Tiles
import net.dv8tion.jda.api.entities.emoji.Emoji

@Serializable
enum class ItemType(
    val emoji: Emoji,
    val maxCount: Int = 16,
    val tileKey: String? = null,
) {
    GRASS(Emojis.getCustom("grass"), tileKey = "grass"),
    DIRT(Emojis.getCustom("dirt"), tileKey = "dirt"),
    STONE(Emojis.getCustom("stone"), tileKey = "stone"),
    IRON_CHUNK(Emojis.getCustom("iron_chunk")),
    ;

    /** The tile this item places when used, or null if it isn't placeable. */
    fun getTile(): Tile? = tileKey?.let { Tiles.getTile(Tiles.getID(it)) }
}
