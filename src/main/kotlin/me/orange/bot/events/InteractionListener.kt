package me.orange.bot.events

import me.orange.bot.MineB0t
import me.orange.bot.events.base.Interaction
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class InteractionListener(private val interactions: List<Interaction>) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        findAndHandle(event.name, event)
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        findAndHandle(event.button.id ?: return, event)
    }

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        findAndHandle(event.selectMenu.id ?: return, event)
    }

    private fun findAndHandle(id: String, event: Any) {
        val interaction = interactions.find { it.matches(id) }
        if (interaction != null) {
            interaction.handle(event)
        } else {
            MineB0t.log("Unhandled interaction: $id")
            if (event is net.dv8tion.jda.api.interactions.callbacks.IReplyCallback) {
                event.reply("This interaction is no longer valid or is not handled by this bot.")
                    .setEphemeral(true).queue({}, {}) // Silent fail if already acknowledged
            }
        }
    }
}
