package me.orange.bot.events.base

import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.build.OptionData

abstract class SlashCommand(
    id: String,
    val description: String,
    val options: List<OptionData> = emptyList(),
    execute: suspend (InteractionHook, Event) -> Unit
) : BaseInteraction(id, { (it as? SlashCommandInteractionEvent)?.name }, edit = false, execute)