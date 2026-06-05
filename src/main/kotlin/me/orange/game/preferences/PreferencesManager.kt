package me.orange.game.preferences

import kotlinx.serialization.json.Json
import me.orange.bot.Config
import me.orange.bot.MineB0t
import me.orange.game.GamesManager
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

object PreferencesManager {
    val playerPreferences: ConcurrentHashMap<Long, ConcurrentHashMap<Preference, Any>> = ConcurrentHashMap()

    fun showMenu(hook: InteractionHook) {
        val playerId = hook.interaction.user.idLong
        val selectionMenu = StringSelectMenu.create("settings")
            .let {
                for (preference in Preference.entries) {
                    val current = getPreference<Any>(playerId, preference).toString()
                    it.addOption(preference.name, preference.name, "Currently: $current")
                }
                it.setPlaceholder("Choose a setting")
            }.build()
        hook.editOriginalComponents(ActionRow.of(selectionMenu)).queue()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getPreference(id: Long, preference: Preference): T {
        val value = playerPreferences[id]?.get(preference)
        return if (value != null) value as T else preference.default as T
    }

    fun getHeadEmoji(id: Long): String {
        val stored = playerPreferences[id]?.get(Preference.HEAD_EMOJI) as? String
        if (stored != null) return stored
        val pool = Preference.HEAD_EMOJI.options
        return pool[Math.floorMod(id, pool.size)] as String
    }

    fun setPreference(id: Long, pref: Preference, value: String) {
        val parsed = parseValue(value, pref.valueType) ?: return
        playerPreferences.getOrPut(id) { ConcurrentHashMap() }[pref] = parsed
    }

    fun setPreference(setting: Preference, hook: InteractionHook, value: String) {
        val playerId = hook.interaction.user.idLong
        val playerName = hook.interaction.user.name
        setPreference(playerId, setting, value)
        savePreferences(playerId)
        MineB0t.log("Player $playerName ($playerId) set ${setting.name} = $value")
        hook.editOriginal("Successfully set ${setting.name} to $value").queue()
        hook.editOriginalComponents().queue()
        val guildId = hook.interaction.guild?.id ?: return
        GamesManager.games[guildId]?.let { game ->
            game.playerEnvUiCache.remove(playerId)
            game.playerCraftUiCache.remove(playerId)
        }
    }

    fun loadPreferences(id: Long, fallback: Map<String, String> = emptyMap()) {
        val file = fileOf(id)
        val raw = if (file.exists()) Json.decodeFromString<Map<String, String>>(file.readText()) else fallback
        if (raw.isEmpty()) return
        val prefMap = ConcurrentHashMap<Preference, Any>()
        raw.forEach { (prefName, value) ->
            val pref = runCatching { Preference.valueOf(prefName) }.getOrNull() ?: return@forEach
            val parsed = parseValue(value, pref.valueType) ?: return@forEach
            prefMap[pref] = parsed
        }
        val existing = playerPreferences[id]
        if (existing != null) prefMap.putAll(existing)
        playerPreferences[id] = prefMap
    }

    fun savePreferences(id: Long) {
        val prefs = playerPreferences[id] ?: return
        val data = prefs.entries.associate { (pref, value) -> pref.name to value.toString() }
        val file = fileOf(id)
        file.parentFile.mkdirs()
        file.writeText(Json.encodeToString(data))
    }

    private fun fileOf(id: Long) = File("${Config.PLAYER_DATA_DIR}/$id.json")

    private val parsers: Map<KClass<*>, (String) -> Any?> = mapOf(
        Int::class to { it.toIntOrNull() },
        Long::class to { it.toLongOrNull() },
        Double::class to { it.toDoubleOrNull() },
        Float::class to { it.toFloatOrNull() },
        Boolean::class to { it.toBooleanStrictOrNull() },
        String::class to { it }
    )

    fun parseValue(value: String, type: KClass<*>): Any? = parsers[type]?.invoke(value)
}
