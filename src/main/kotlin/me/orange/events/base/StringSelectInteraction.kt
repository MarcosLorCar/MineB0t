package me.orange.events.base

import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook

abstract class StringSelectInteraction(
    id: String,
    execute: suspend (InteractionHook, Event) -> Unit
) : BaseInteraction(id, { (it as? StringSelectInteractionEvent)?.selectMenu?.id }, edit = true, execute)