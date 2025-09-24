package me.orange.game.preferences

import me.orange.game.Game
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import kotlin.reflect.KClass

class PreferencesManager(game: Game) {
    val playerPreferences = mutableMapOf<Long, MutableMap<Preference, Any>>()

    fun showMenu(it: InteractionHook) {
        //Show a menu with all preferences identified with a number
        // then implement /settings <id> <value>
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
        val entry = playerPreferences[id]
        val value = entry?.get(preference)
        return if (value != null) value as T else preference.default as T
    }

    fun setPreference(setting: Preference, it: InteractionHook, value: String) {
        val id = it.interaction.user.idLong
        val entry = playerPreferences.getOrPut(id) { mutableMapOf() }
        entry[setting] = parseValue(value, setting.valueType)!!
        it.editOriginal("Successfully set ${setting.name} to $value").queue()
        it.editOriginalComponents().queue()
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