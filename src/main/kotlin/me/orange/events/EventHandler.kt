package me.orange.events

import me.orange.events.commands.PlayCommand
import me.orange.events.commands.TestCommand
import me.orange.events.interactions.InputInteraction
import me.orange.events.interactions.PlayInteraction
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.Commands

object EventHandler {
    private val commands = listOf(
        PlayCommand,
        TestCommand,
    )

    private val interactions = mutableListOf(
        InputInteraction("move_left"),
        InputInteraction("move_right"),
        InputInteraction("changeMode_place"),
        InputInteraction("changeMode_break"),
        InputInteraction("action_up_left"),
        InputInteraction("action_up_up"),
        InputInteraction("action_up_right"),
        InputInteraction("action_left"),
        InputInteraction("action_right"),
        InputInteraction("action_down_left"),
        InputInteraction("action_down"),
        InputInteraction("action_down_right"),
        InputInteraction("inventory_open"),
        InputInteraction("inventory_left"),
        InputInteraction("inventory_right"),
        InputInteraction("inventory_close"),
        PlayInteraction
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