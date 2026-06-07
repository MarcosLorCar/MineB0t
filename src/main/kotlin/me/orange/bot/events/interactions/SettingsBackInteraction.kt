package me.orange.bot.events.interactions

import me.orange.bot.events.base.ButtonInteraction
import me.orange.game.preferences.PreferencesManager

object SettingsBackInteraction : ButtonInteraction(
    id = "settings_back",
    execute = { hook, _ ->
        PreferencesManager.showMenu(hook)
    }
)