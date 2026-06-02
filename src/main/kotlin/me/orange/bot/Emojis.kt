package me.orange.bot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.Emoji

object Emojis {
    val customEmoji: MutableMap<String, Triple<String, Long, Boolean>> = mutableMapOf()
    val emojis: MutableMap<String, Emoji> = mutableMapOf(
        "break" to Emoji.fromUnicode("⛏\uFE0F"),
        "place" to Emoji.fromUnicode("\uD83E\uDEF3"),
        "null" to Emoji.fromUnicode("❌"),
        "other_head" to Emoji.fromUnicode("\uD83D\uDC7D"),
        "sleepy_head" to Emoji.fromUnicode("\uD83D\uDE34"),
        "group_head" to Emoji.fromUnicode("\uD83D\uDC65"),
        "other_body" to Emoji.fromUnicode("\uD83E\uDDBA"),
        "return" to Emoji.fromUnicode("↩\uFE0F"),
        "selected" to Emoji.fromUnicode("\uD83D\uDD3C"),
        "info" to Emoji.fromUnicode("\u2139\uFE0F"),
        // Crafting station / item fallbacks (no custom Discord emoji yet)
        "crafting_table" to Emoji.fromUnicode("\uD83D\uDEE0\uFE0F"),
        "furnace" to Emoji.fromUnicode("\uD83C\uDFED"),
        "iron_ingot" to Emoji.fromUnicode("\uD83D\uDD29"),
        // TODO: replace with custom Discord emojis once uploaded
        "coal" to Emoji.fromUnicode("\u26AB"),
    )

    fun loadEmojis() {
        MineB0t.log("Loading emojis")

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
        loadEmoji("craft_icon", "_", 1369251709557149716)

        // Block emojis
        loadEmoji("air", "_", 1511480218806911057)
        loadEmoji("dirt", "_", 1511485086409691238)
        loadEmoji("dirt_2", "_", 1511485099512827977)
        loadEmoji("grass", "_", 1511481157680894143)
        loadEmoji("stone", "_", 1511481647504167043)
        loadEmoji("stone_2", "_", 1511482399719034971)
        loadEmoji("iron_ore", "_", 1511494463191515156)
        loadEmoji("iron_ore_1", "_", 1511495090286235780)
        loadEmoji("iron_ore_2", "_", 1511495130794692890)
        loadEmoji("iron_ore_3", "_", 1511497061210656868)
        loadEmoji("iron_ore_4", "_", 1511497113618485292)
        loadEmoji("iron_ore_5", "_", 1511497167993569430)
        loadEmoji("coal_ore", "_", 1511478214466211971)


        // Item emojis
        loadEmoji("iron_chunk", "iron_chunk", 1365268724118065224)
    }

    fun loadEmoji(name: String, nameId: String, id: Long, animated: Boolean = false) =
        customEmoji.put(name, Triple(nameId, id, animated))

    /**
     * Checks every loaded custom-emoji ID against the bot's application emojis and cached guild
     * emojis. Any ID that resolves to neither will render as raw `<:name:id>` text in Discord
     * (e.g. a typo'd / outdated ID), so we log it loudly at startup to make it obvious.
     */
    fun validate(jda: JDA) {
        val appEmojiIds = runCatching {
            jda.retrieveApplicationEmojis().complete().map { it.idLong }.toSet()
        }.getOrElse {
            MineB0t.log("Could not retrieve application emojis for validation: ${it.message}")
            emptySet()
        }

        val broken = customEmoji.filterValues { (_, id, _) ->
            id !in appEmojiIds && jda.getEmojiById(id) == null
        }

        if (broken.isEmpty()) {
            MineB0t.log("All ${customEmoji.size} custom emojis resolved successfully")
        } else {
            broken.forEach { (key, data) ->
                MineB0t.log("⚠ Emoji '$key' (id=${data.second}) resolves to no application or cached guild emoji — it will render as raw text. Check the ID in Emojis.loadEmojis().")
            }
        }
    }

    fun get(name: String): Emoji =
        if (customEmoji.containsKey(name)) {
            val d = customEmoji[name]!!
            Emoji.fromCustom(d.first, d.second, d.third)
        } else {
            emojis[name]!!
        }

    fun getFormatted(name: String): String = get(name).formatted

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
        else -> getFormatted("null")
    }
}