package me.orange.bot

import kotlinx.coroutines.*
import me.orange.events.EventHandler
import me.orange.game.GamesManager
import me.orange.game.world.tile.Tiles
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MineB0t {

    val supervisorJob = SupervisorJob()

    val scope = CoroutineScope(Dispatchers.Default + supervisorJob)

    private val logger: Logger = LoggerFactory.getLogger(MineB0t::class.java)

    private lateinit var jda: JDA

    fun start() = runBlocking {
        val token = System.getenv("DISCORD_BOT_TOKEN") ?: error("Missing token!")
        jda = JDABuilder.createDefault(token)
            .build()

        jda.awaitReady()

        // Updates the command list and registers listeners
        EventHandler.registerEvents(jda)

        Emojis.loadEmojis()

        // Register tiles
        log("Registering tiles")
        Tiles

        // Launch command listener
        startCommandListener()
    }

    fun stop() {
        GamesManager.saveAll()
        jda.shutdown()
        logger.info("Bot stopped")
    }

    fun startCommandListener() = scope.launch {
        log("Listening for commands. Type 'stop' to stop the bot.")
        while (true) {
            val command = readln()
            when (command) {
                "stop" -> {
                    println("Stopping bot...")
                    stop()
                    break
                }
                else -> println("Unknown command: $command")
            }
        }
    }

    fun launch(block: suspend CoroutineScope.() -> Unit) = scope.launch(block = block)
    fun log(msg: String) = logger.info(msg)
}