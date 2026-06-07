package me.orange.bot.events.interactions

import me.orange.bot.events.base.StringSelectInteraction
import me.orange.game.GamesManager
import me.orange.game.preferences.Preference
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

class ChangeSettingInteraction(val setting: Preference) : StringSelectInteraction(
    id = setting.name,
    execute = { hook, event ->
        val selectEvent = event as StringSelectInteractionEvent
        val game = GamesManager.getGame(selectEvent.guild!!.id)
        game.preferencesManager.setPreference(setting, hook, selectEvent.selectedOptions.first().value)
    }
)