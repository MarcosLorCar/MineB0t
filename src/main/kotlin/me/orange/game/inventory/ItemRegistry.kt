package me.orange.game.inventory

import net.dv8tion.jda.api.entities.emoji.Emoji

object ItemRegistry {
    private val items = mutableListOf<Item>()
    private val keyMap = mutableMapOf<String, Item>()

    fun register(key: String, emoji: Emoji, block: Item.Builder.() -> Unit): Item {
        val item = Item.Builder(key, emoji).apply(block).build()
        items.add(item)
        keyMap[key] = item
        return item
    }

    fun get(key: String): Item = keyMap[key] ?: error("Unknown item key '$key'")
    val count: Int get() = items.size
}
