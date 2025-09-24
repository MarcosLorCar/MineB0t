package me.orange.events.base

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

abstract class StringSelectInteraction(
    val id: String,
) : ListenerAdapter() {
    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        if (id == event.selectMenu.id) execute(event)
    }

    abstract fun execute(event: StringSelectInteractionEvent)
}