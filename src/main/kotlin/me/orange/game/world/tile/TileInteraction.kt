package me.orange.game.world.tile

import me.orange.bot.Emojis
import me.orange.game.craft.CraftingStationType
import net.dv8tion.jda.api.entities.emoji.Emoji

sealed class TileInteraction {
    object None : TileInteraction()
    data class CraftingStation(val type: CraftingStationType) : TileInteraction()
    object Chest : TileInteraction()

    val emoji: Emoji get() = when (this) {
        is None -> Emojis.get("crafting_table")
        is CraftingStation -> type.emoji
        is Chest -> Emojis.get("chest")
    }

    val buttonId: String get() = when (this) {
        is None, is CraftingStation -> "craft_open"
        is Chest -> "chest_open"
    }
}
