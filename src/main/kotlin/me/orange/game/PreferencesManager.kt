package me.orange.game

import net.dv8tion.jda.api.interactions.InteractionHook

class PreferencesManager(game: Game) {
    fun showMenu(it: InteractionHook) {
        TODO("Not yet implemented")
    }

    val playerPreferences = mutableMapOf<Long, MutableMap<String, Any>>()
}
