package me.orange.events

import me.orange.bot.MineB0t
import me.orange.events.commands.PlayCommand
import me.orange.events.commands.PreferencesCommand
import me.orange.events.commands.SetHeadCommand
import me.orange.events.commands.TestCommand
import me.orange.events.interactions.ChangeSettingInteraction
import me.orange.events.interactions.CraftSelectInteraction
import me.orange.events.interactions.InputInteraction
import me.orange.events.interactions.PlayInteraction
import me.orange.events.interactions.SelectSettingInteraction
import me.orange.game.preferences.Preference
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands

object EventHandler {
    private val commands = listOf(
        PlayCommand,
        TestCommand,
        PreferencesCommand,
        SetHeadCommand
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
        InputInteraction("craft_close"),
        InputInteraction("craft_prev"),
        InputInteraction("craft_next"),
        PlayInteraction,
        SelectSettingInteraction,
        CraftSelectInteraction,
    )

    fun registerEvents(jda: JDA) {
        MineB0t.log("Registering events")

        // Slash commands
        commands.forEach(jda::addEventListener)
        jda.addEventListener(RegisterCommandsListener)

        // interactions
        interactions.forEach(jda::addEventListener)

        // ChangeSetting Interactions
        Preference.entries.forEach { pref ->
            jda.addEventListener(ChangeSettingInteraction(pref))
        }

    }

    object RegisterCommandsListener : ListenerAdapter() {
        override fun onGuildReady(event: GuildReadyEvent) {
            registerCommands(event.guild)
        }

        override fun onGuildJoin(event: GuildJoinEvent) {
            registerCommands(event.guild)
        }

        private fun registerCommands(guild: Guild) {
            val updateCommands = guild.updateCommands()

            commands.forEach { command ->
                // Register signature
                updateCommands.addCommands(Commands.slash(
                    command.id,
                    command.description
                ).addOptions(command.options))
            }

            updateCommands.queue()
        }
    }
}