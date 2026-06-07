package me.orange.bot.events.interactions

import me.orange.bot.events.base.StringSelectInteraction
import me.orange.game.preferences.Preference
import me.orange.game.preferences.PreferencesManager
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

object SelectSettingInteraction : StringSelectInteraction(
    id = "settings",
    execute = { hook, event ->
        val setting = Preference.valueOf((event as StringSelectInteractionEvent).selectedOptions.first().value)
        PreferencesManager.showSettingMenu(hook, setting)
    }
)