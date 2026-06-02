package me.orange.bot.events.base

import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook

abstract class ButtonInteraction(
    id: String,
    execute: suspend (InteractionHook, Event) -> Unit
) : BaseInteraction(id, { (it as? ButtonInteractionEvent)?.button?.id }, edit = true, execute)