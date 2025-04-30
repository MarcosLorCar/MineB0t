package me.orange.bot

import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.entities.emoji.Emoji

object Emojis {
    val customEmoji: MutableMap<String, Triple<String, Long, Boolean>> = mutableMapOf()
    val emojis: MutableMap<String, Emoji> = mutableMapOf(
        "break" to Emoji.fromUnicode("⛏\uFE0F"),
        "place" to Emoji.fromUnicode("\uD83E\uDEF3"),
        "null" to Emoji.fromUnicode("❌"),
        "other_head" to Emoji.fromUnicode("\uD83D\uDC7D"),
        "sleepy_head" to Emoji.fromUnicode("\uD83D\uDE34"),
        "other_body" to Emoji.fromUnicode("\uD83E\uDDBA"),
        "return" to Emoji.fromUnicode("↩\uFE0F"),
        "selected" to Emoji.fromUnicode("\uD83D\uDD3C"),
    )

    fun loadEmojis() {
        // Ui emojis
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
        loadEmoji("backpack", "backpack", 1355610066702827741)

        // Block emojis
        loadEmoji("air", "_", 1356533932765741216)
        loadEmoji("dirt", "_", 1356538992111128657)
        loadEmoji("grass", "_", 1356538859487236231)
        loadEmoji("stone", "_", 1356534787745255524)
        loadEmoji("iron_ore", "_", 1365275718006866011)

        // Item emojis
        loadEmoji("iron_chunk", "iron_chunk", 1365268724118065224)
    }

    fun loadEmoji(name: String, nameId: String, id: Long, animated: Boolean = false) =
        customEmoji.put(name, Triple(nameId, id, animated))

    fun getCustom(name: String): CustomEmoji {
        val emojiData = customEmoji[name]!!
        return Emoji.fromCustom(emojiData.first, emojiData.second, emojiData.third)
    }

    fun getEmoji(name: String) = emojis[name]!!
    fun getEmojiCode(name: String): String = emojis[name]!!.formatted

    fun getNumber(i: Int) = when (i) {
        0 -> "0\uFE0F⃣"
        1 -> "1\uFE0F⃣"
        2 -> "2\uFE0F⃣"
        3 -> "3\uFE0F⃣"
        4 -> "4\uFE0F⃣"
        5 -> "5\uFE0F⃣"
        6 -> "6\uFE0F⃣"
        7 -> "7\uFE0F⃣"
        8 -> "8\uFE0F⃣"
        9 -> "9\uFE0F⃣"
        else -> getEmoji("null").formatted
    }
}