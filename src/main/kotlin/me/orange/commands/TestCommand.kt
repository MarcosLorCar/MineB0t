package me.orange.commands

import me.orange.commands.base.SlashCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object TestCommand : SlashCommand(
    name = "test",
    description = "This is a test command"
) {

    override fun execute(event: SlashCommandInteractionEvent) {
        event.reply("TEST").queue()
    }
}