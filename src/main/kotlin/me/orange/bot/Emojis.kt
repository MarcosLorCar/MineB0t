package me.orange.bot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.emoji.Emoji

object Emojis {
    val customEmoji: MutableMap<String, Triple<String, Long, Boolean>> = mutableMapOf()
    val emojis: MutableMap<String, Emoji> = mutableMapOf(
        "break" to Emoji.fromUnicode("⛏️"),
        "place" to Emoji.fromUnicode("🫳"),
        "null" to Emoji.fromUnicode("❌"),
        "other_head" to Emoji.fromUnicode("👽"),
        "sleepy_head" to Emoji.fromUnicode("😴"),
        "group_head" to Emoji.fromUnicode("👥"),
        "other_body" to Emoji.fromUnicode("🦺"),
        "return" to Emoji.fromUnicode("↩️"),
        "selected" to Emoji.fromUnicode("◀️"),
        "info" to Emoji.fromUnicode("ℹ️"),
        "store" to Emoji.fromUnicode("📥"),
        "retrieve" to Emoji.fromUnicode("📤"),
        // Crafting station / item fallbacks (no custom Discord emoji yet)
        "crafting_table" to Emoji.fromUnicode("🛠️"),
        "furnace" to Emoji.fromUnicode("🏭"),
        "iron_ingot" to Emoji.fromUnicode("🔩"),
    )

    fun loadEmojis() {
        MineB0t.log("Loading emojis")

        // Ui emojis
        loadEmoji("move_right", "play_button", 1352675305218637844)
        loadEmoji("move_left", "reverse_button", 1352675269202415626)
        loadEmoji("up_left", "up_left_arrow", 1352674935981604894)
        loadEmoji("up", "up_arrow", 1352674885016485899)
        loadEmoji("up_right", "up_right_arrow", 1352674845002961008)
        loadEmoji("right_and_up_right", "dual_right_arrow", 1512409356765761566)
        loadEmoji("left_and_up_left", "dual_left_arrow", 1512409355138629733)
        loadEmoji("left", "left_arrow", 1352674802053283860)
        loadEmoji("right", "right_arrow", 1352674713029050420)
        loadEmoji("down_left", "down_left_arrow", 1352674684931543160)
        loadEmoji("down", "down_arrow", 1352674630384484372)
        loadEmoji("down_right", "down_right_arrow", 1352674531529199737)
        loadEmoji("backpack", "backpack", 1512929819653046582)
        loadEmoji("craft_icon", "_", 1369251709557149716)

        // Block emojis
        loadEmoji("air", "_", 1511480218806911057)

        loadEmoji("dirt", "_", 1512116994156138667)
        loadEmoji("dirt_1", "_", 1512116995888381992)
        loadEmoji("dirt_2", "_", 1512116996958064824)

        loadEmoji("grass", "_", 1512125506114552009)
        loadEmoji("grass_1", "_", 1512125507477573783)
        loadEmoji("grass_2", "_", 1512126503247151185)

        loadEmoji("stone", "_", 1512111539551862944)
        loadEmoji("stone_2", "_", 1512111541028388895)

        loadEmoji("iron_ore", "_", 1512121554392322118)
        loadEmoji("iron_ore_1", "_", 1512121555440762941)

        loadEmoji("coal_ore", "_", 1512124423623409855)
        loadEmoji("coal_ore_1", "_", 1512124424588103842)

        loadEmoji("red_shroom_tile", "_", 1512378160115875880)
        loadEmoji("red_shroom_tile_1", "_", 1512378161294475367)

        loadEmoji("log_tile", "_", 1513164648227016865)
        loadEmoji("leaves", "_", 1513164646721523813)
        loadEmoji("canopy", "_", 1513164645534400735)

        loadEmoji("chest", "_", 1512553817298702347)

        // Item emojis
        loadEmoji("iron_chunk", "iron_chunk", 1512391416322719804)
        loadEmoji("coal", "coal", 1512391415265628200)
        loadEmoji("log", "_", 1513171531717673011)
        loadEmoji("red_shroom", "red_shroom", 1512378159054717009)
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
        0 -> "0️⃣"
        1 -> "1️⃣"
        2 -> "2️⃣"
        3 -> "3️⃣"
        4 -> "4️⃣"
        5 -> "5️⃣"
        6 -> "6️⃣"
        7 -> "7️⃣"
        8 -> "8️⃣"
        9 -> "9️⃣"
        else -> getFormatted("null")
    }

    fun renderCountEmoji(count: Int): String {
        val digits = count.toString().map { c -> getNumber(c.digitToInt()) }
        return if (digits.size == 1) "${digits[0]}${getFormatted("air")}" else digits.joinToString("")
    }

    fun renderCountText(count: Int): String = " $count"

    fun renderStack(itemEmoji: String, count: Int): String = "**${count}x** $itemEmoji"

    fun renderSemiStack(itemEmoji: String, have: Int, need: Int): String = "**($have/$need)x** $itemEmoji"
}
