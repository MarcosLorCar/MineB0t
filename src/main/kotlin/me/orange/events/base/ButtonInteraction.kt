package me.orange.events.base

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

abstract class ButtonInteraction(
    val id: String,
) : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.button.id == id) execute(event)
    }

    abstract fun execute(event: ButtonInteractionEvent)
}