package me.orange.events

import me.orange.events.commands.PlayCommand
import me.orange.events.commands.PreferencesCommand
import me.orange.events.commands.TestCommand
import me.orange.events.interactions.ChangeSettingInteraction
import me.orange.events.interactions.InputInteraction
import me.orange.events.interactions.PlayInteraction
import me.orange.events.interactions.SelectSettingInteraction
import me.orange.game.preferences.Preference
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.interactions.commands.build.Commands

object EventHandler {
    private val commands = listOf(
        PlayCommand,
        TestCommand,
        PreferencesCommand
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
        InputInteraction("craft_open"),
        PlayInteraction,
        SelectSettingInteraction,
    )

    fun registerEvents(jda: JDA) {
        val updateCommands = jda.updateCommands()

        // Slash commands
        commands.forEach { command ->
            // Register signature
            updateCommands.addCommands(
                Commands.slash(command.name, command.description)
            )
        }
        commands.forEach(jda::addEventListener)

        // interactions
        interactions.forEach(jda::addEventListener)

        // ChangeSetting Interactions
        Preference.entries.forEach { pref ->
            jda.addEventListener(ChangeSettingInteraction(pref))
        }

        updateCommands.queue()
    }
}