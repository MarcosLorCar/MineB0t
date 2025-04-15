package me.orange.bot

import me.orange.events.EventHandler
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MineB0t {

    private val logger: Logger = LoggerFactory.getLogger(MineB0t::class.java)

    private lateinit var jda: JDA

    fun start() {
        val token = System.getenv("DISCORD_BOT_TOKEN") ?: error("Missing token!")
        jda = JDABuilder.createDefault(token)
            .build()

        // Updates the commands list and resisters listeners
        EventHandler.registerEvents(jda)

        Emojis.loadEmojis()
    }

    fun log(msg: String) = logger.info(msg)
}