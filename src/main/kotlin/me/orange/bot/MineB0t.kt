package me.orange.bot

import kotlinx.coroutines.*
import me.orange.events.EventHandler
import me.orange.game.GamesManager
import me.orange.game.craft.Recipes
import me.orange.game.inventory.Items
import me.orange.game.world.tile.Tiles
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object MineB0t {

    val supervisorJob = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Default + supervisorJob)
    private val logger: Logger = LoggerFactory.getLogger(MineB0t::class.java)
    private lateinit var jda: JDA

    fun start() = runBlocking {
        val token = System.getenv("DISCORD_BOT_TOKEN") ?: error("Missing token!")
        jda = JDABuilder.createDefault(token).build()
        jda.awaitReady()

        Runtime.getRuntime().addShutdownHook(Thread {
            stop()
        })

        EventHandler.registerEvents(jda)
        Emojis.loadEmojis()
        Emojis.validate(jda)

        log("Registering tiles: ${Tiles.count} registered")
        log("Registering items: ${Items.count} registered")
        log("Registering recipes: ${Recipes.count} registered")

        val listenerJob = startCommandListener()
        listenerJob.join()
    }

    fun stop() {
        synchronized(this) {
            if (!::jda.isInitialized) return

            GamesManager.saveAll()
            jda.shutdown()
            logger.info("Bot stopped")
            supervisorJob.cancel()
        }
    }

    fun startCommandListener(): Job = scope.launch(Dispatchers.IO) {
        log("Listening for commands. Type 'stop' to stop the bot.")
        try {
            while (isActive) {
                when (val command = readlnOrNull() ?: break) {
                    "stop" -> {
                        println("Stopping bot...")
                        exitProcess(0)
                    }
                    else -> println("Unknown command: $command")
                }
            }
        } catch (_: Exception) {
            logger.debug("Console reader stream closed.")
        }
    }

    fun launch(block: suspend CoroutineScope.() -> Unit) = scope.launch(block = block)
    fun log(msg: String) = logger.info(msg)
}