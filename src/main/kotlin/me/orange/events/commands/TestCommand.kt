package me.orange.events.commands

import me.orange.events.base.SlashCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

object TestCommand : SlashCommand(
    name = "test",
    description = "This is a test command"
) {

    override fun execute(event: SlashCommandInteractionEvent) {
        val embed = EmbedBuilder()
            .addField(MessageEmbed.Field("⬛⬛⬛⬛⬛\n⬛⬛⬛⬛⬛", "⬛⬛⬛⬛⬛\n⬛⬛⬛⬛⬛", false))

        event.replyEmbeds(embed.build()).queue()
    }
}