package me.orange.events.interactions

import me.orange.events.base.StringSelectInteraction
import me.orange.game.GamesManager
import me.orange.game.preferences.Preference
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

object SelectSettingInteraction : StringSelectInteraction(
    id = "settings",
    execute = { hook, event ->
        val game = GamesManager.getGame((event as StringSelectInteractionEvent).guild!!.id)
        val setting = Preference.valueOf(event.selectedOptions.first().value)
        val newStringSelectMenu =
            StringSelectMenu.create(setting.name)
                .addOptions(setting.options.map {
                    SelectOption.of(it.toString(), it.toString())
                })
                .build()
        hook.editOriginal("Choose a new value for ${setting.name}")
            .setActionRow(newStringSelectMenu).queue()
    }
)