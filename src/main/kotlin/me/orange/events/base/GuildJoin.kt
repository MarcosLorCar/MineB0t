package me.orange.events.base

import me.orange.events.EventHandler
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GuildJoin : ListenerAdapter() {
    override fun onGuildJoin(event: GuildJoinEvent) {
        EventHandler.registerCommands(event.guild)
    }
}