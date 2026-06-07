package me.orange.game.craft

import me.orange.bot.Emojis
import net.dv8tion.jda.api.entities.emoji.Emoji

enum class CraftingStationType(val emoji: Emoji) {
    NONE(Emojis.get("crafting_table")),
    CRAFTING_TABLE(Emojis.get("crafting_table")),
    FURNACE(Emojis.get("furnace")),
}