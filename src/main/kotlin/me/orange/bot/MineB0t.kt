package me.orange.bot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.orange.events.EventHandler
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MineB0t {

    val supervisorJob = SupervisorJob()

    val scope = CoroutineScope(Dispatchers.Default + supervisorJob)

    private val logger: Logger = LoggerFactory.getLogger(MineB0t::class.java)

    private lateinit var jda: JDA

    fun start() {
        val token = System.getenv("DISCORD_BOT_TOKEN") ?: error("Missing token!")
        jda = JDABuilder.createDefault(token)
            .build()

        // Updates the command list and resisters listeners
        EventHandler.registerEvents(jda)

        Emojis.loadEmojis()
    }

    fun launch(block: suspend CoroutineScope.() -> Unit) = scope.launch(block = block)

    fun log(msg: String) = logger.info(msg)
}