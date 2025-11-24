package me.orange.events.base

import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook

abstract class SlashCommand(
    id: String,
    val description: String,
    execute: suspend (InteractionHook, Event) -> Unit
) : BaseInteraction(id, { (it as? SlashCommandInteractionEvent)?.name }, edit = false, execute)