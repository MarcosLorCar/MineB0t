package me.orange.game.preferences

import me.orange.game.Game
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class PreferencesManager(private val game: Game) {
    val playerPreferences: ConcurrentHashMap<Long, ConcurrentHashMap<Preference, Any>> = ConcurrentHashMap()

    fun showMenu(it: InteractionHook) {
        val selectionMenu = StringSelectMenu.create("settings")
            .let {
                for (preference in Preference.entries) {
                    it.addOption(preference.name, preference.name)
                }
                it.setPlaceholder("Choose a setting")
            }.build()
        it.editOriginalComponents(ActionRow.of(selectionMenu)).queue()
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
        setPreference(hook.interaction.user.idLong, setting, value)
        hook.editOriginal("Successfully set ${setting.name} to $value").queue()
        hook.editOriginalComponents().queue()
    }

    fun loadPlayerPreferences(id: Long, prefs: Map<String, String>) {
        if (prefs.isEmpty()) return
        val prefMap = ConcurrentHashMap<Preference, Any>()
        prefs.forEach { (prefName, value) ->
            val pref = runCatching { Preference.valueOf(prefName) }.getOrNull() ?: return@forEach
            val parsed = parseValue(value, pref.valueType) ?: return@forEach
            prefMap[pref] = parsed
        }
        playerPreferences[id] = prefMap
    }

    val parsers: Map<KClass<*>, (String) -> Any?> = mapOf(
        Int::class to { it.toIntOrNull() },
        Long::class to { it.toLongOrNull() },
        Double::class to { it.toDoubleOrNull() },
        Float::class to { it.toFloatOrNull() },
        Boolean::class to { it.toBooleanStrictOrNull() },
        String::class to { it }
    )

    fun parseValue(value: String, type: KClass<*>): Any? =
        parsers[type]?.invoke(value)
}
