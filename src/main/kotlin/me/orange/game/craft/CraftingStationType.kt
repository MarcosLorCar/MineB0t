package me.orange.game.craft

/**
 * A station required to craft certain recipes. [emojiKey] resolves through [me.orange.bot.Emojis.get]
 * (custom-or-unicode) and themes both the world Craft button and the crafting view.
 */
enum class CraftingStationType(val emojiKey: String) {
    NONE("craft_icon"),
    CRAFTING_TABLE("crafting_table"),
    FURNACE("furnace"),
}
