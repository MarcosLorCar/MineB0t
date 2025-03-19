package me.orange.commands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.Commands

object CommandHandler {
    private val commands = listOf(
        PlayCommand,
        TestCommand,
    )

    private val interactions = listOf(
        MoveInteraction("left"),
        MoveInteraction("down"),
        MoveInteraction("right"),
        ChangeModeInteraction("place"),
        ChangeModeInteraction("break"),
        ChangeModeInteraction("move"),
        ActionInteraction("break_left"),
        ActionInteraction("break_down"),
        ActionInteraction("break_right"),
        ActionInteraction("break_up"),
        ActionInteraction("place_left"),
        ActionInteraction("place_down"),
        ActionInteraction("place_right"),
        ActionInteraction("place_up"),
    )

    fun registerEvents(jda: JDA) {
        val updateCommands = jda.updateCommands()

        commands.forEach { command ->
            // Register signature
            updateCommands.addCommands(Commands.slash(command.name, command.description))

            // Add a listener
            jda.addEventListener(command)
        }

        interactions.forEach(jda::addEventListener)

        updateCommands.queue()
    }
}