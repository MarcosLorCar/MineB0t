package me.orange.game.craft

import me.orange.bot.Emojis
import net.dv8tion.jda.api.entities.emoji.Emoji

enum class CraftingStationType(val emoji: Emoji) {
    NONE(Emojis.get("crafting")),
    FURNACE(Emojis.get("furnace")),
}