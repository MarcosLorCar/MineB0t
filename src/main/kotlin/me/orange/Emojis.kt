package me.orange

import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.Emoji

object Emojis {
    val customEmoji: MutableMap<String, Triple<String, Long, Boolean>> = mutableMapOf()
    val emojis: MutableMap<String, Emoji> = mutableMapOf(
        "break" to Emoji.fromUnicode("⛏\uFE0F"),
        "place" to Emoji.fromUnicode("\uD83E\uDEF3"),
        "null" to Emoji.fromUnicode("❌"),
        "other_head" to Emoji.fromUnicode("\uD83D\uDC7D"),
        "other_body" to Emoji.fromUnicode("\uD83E\uDDBA"),
    )

    fun loadEmojis() {
        loadEmoji("move_right", "play_button", 1352675305218637844)
        loadEmoji("move_left", "reverse_button", 1352675269202415626)
        loadEmoji("up_left", "up_left_arrow", 1352674935981604894)
        loadEmoji("up", "up_arrow", 1352674885016485899)
        loadEmoji("up_right", "up_right_arrow", 1352674845002961008)
        loadEmoji("left", "left_arrow", 1352674802053283860)
        loadEmoji("right", "right_arrow", 1352674713029050420)
        loadEmoji("down_left", "down_left_arrow", 1352674684931543160)
        loadEmoji("down", "down_arrow", 1352674630384484372)
        loadEmoji("down_right", "down_right_arrow", 1352674531529199737)
        loadEmoji("air", "_", 1352306492723695736)
        loadEmoji("dirt", "_", 1352297912515694655)
        loadEmoji("grass", "_", 1352306218278064218)
    }

    fun loadEmoji(name: String, nameId: String, id: Long, animated: Boolean = false) =
        customEmoji.put(name, Triple(nameId, id, animated))

    fun getCustom(name: String): CustomEmoji {
        val emojiData = customEmoji[name]!!
        return Emoji.fromCustom(emojiData.first, emojiData.second, emojiData.third)
    }

    fun getEmoji(name: String) = emojis[name]!!
    fun getEmojiCode(name: String): String = emojis[name]!!.formatted
}