package me.orange.game.preferences

import me.orange.game.Game
import net.dv8tion.jda.api.interactions.InteractionHook

class PreferencesManager(game: Game) {
    val playerPreferences = mutableMapOf<Long, MutableMap<Preference, Any>>()

    fun showMenu(it: InteractionHook) {
        //Show a menu with all preferences identified with a number
        // then implement /settings <id> <value>

    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getPreference(id: Long, preference: Preference): T? {
        val entry = playerPreferences[id]
        val value = entry?.get(preference)
        return if (value != null) value as T else PreferenceDefaults.valueOf(preference.name).value as T
    }
}