package me.orange.events

import me.orange.events.commands.PlayCommand
import me.orange.events.commands.TestCommand
import me.orange.events.interactions.ActionInteraction
import me.orange.events.interactions.ChangeModeInteraction
import me.orange.events.interactions.MoveInteraction
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.Commands

object EventHandler {
    private val commands = listOf(
        PlayCommand,
        TestCommand,
    )

    private val interactions = mutableListOf(
        MoveInteraction("left"),
        MoveInteraction("down"),
        MoveInteraction("right"),
        ChangeModeInteraction("place"),
        ChangeModeInteraction("break"),
        ChangeModeInteraction("move"),
        ActionInteraction("up_left"),
        ActionInteraction("up_up"),
        ActionInteraction("up_right"),
        ActionInteraction("left"),
        ActionInteraction("right"),
        ActionInteraction("down_left"),
        ActionInteraction("down"),
        ActionInteraction("down_right"),
    )

    fun registerEvents(jda: JDA) {
        val updateCommands = jda.updateCommands()

        commands.forEach { command ->
            // Register signature
            updateCommands.addCommands(
                Commands.slash(command.name, command.description)
            )

            // Add a listener
            jda.addEventListener(command)
        }

        interactions.forEach(jda::addEventListener)

        updateCommands.queue()
    }
}