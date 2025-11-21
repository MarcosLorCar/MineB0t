package me.orange.events.interactions

import me.orange.bot.MineB0t
import me.orange.events.base.StringSelectInteraction
import me.orange.game.GamesManager
import me.orange.game.preferences.Preference
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

class ChangeSettingInteraction(val setting: Preference) : StringSelectInteraction(setting.name) {
    override fun execute(
        event: StringSelectInteractionEvent
    ) {
        event.deferEdit().queue {
            val game = GamesManager.getGame(event.guild?.id ?: return@queue)
            MineB0t.launch {
                game.preferencesManager.setPreference(setting, it, event.selectedOptions.first().value)
            }
        }
    }

}