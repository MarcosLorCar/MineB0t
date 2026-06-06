package me.orange.bot.events.interactions

import me.orange.bot.events.base.StringSelectInteraction
import me.orange.game.preferences.Preference
import me.orange.game.preferences.PreferencesManager
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

object SelectSettingInteraction : StringSelectInteraction(
    id = "settings",
    execute = { hook, event ->
        val setting = Preference.valueOf((event as StringSelectInteractionEvent).selectedOptions.first().value)
        val playerId = event.user.idLong
        val current = PreferencesManager.getEffectivePreference(playerId, setting).toString()
        val newStringSelectMenu =
            StringSelectMenu.create(setting.name)
                .addOptions(setting.options.map {
                    SelectOption.of(it.toString(), it.toString())
                })
                .build()
        hook.editOriginal("Currently: $current\nChoose a new value for ${setting.name}")
            .setActionRow(newStringSelectMenu).queue()
    }
)