package me.orange.events.commands.base

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

abstract class SlashCommand(
    val name: String,
    val description: String
) : ListenerAdapter() {
    abstract fun execute(event: SlashCommandInteractionEvent)

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == name) execute(event)
    }
}