package me.orange.game.inventory

import kotlinx.serialization.Serializable
import me.orange.bot.Emojis
import me.orange.game.world.TileType
import net.dv8tion.jda.api.entities.emoji.Emoji

@Serializable
enum class ItemType(
    val emoji: Emoji,
    val maxCount: Int = 16,
    val getTileType: () -> TileType = { TileType.NULL }
) {
    GRASS(Emojis.getCustom("grass"), getTileType = { TileType.GRASS }),
    DIRT(Emojis.getCustom("dirt"), getTileType = { TileType.DIRT }),
    STONE(Emojis.getCustom("stone"), getTileType = { TileType.STONE }),
    IRON_CHUNK(Emojis.getCustom("iron_chunk")),
}
