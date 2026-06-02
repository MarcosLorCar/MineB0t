package me.orange.bot

import kotlinx.coroutines.*
import me.orange.console.ConsoleCommandHandler
import me.orange.bot.events.EventHandler
import me.orange.game.GamesManager
import me.orange.game.craft.Recipes
import me.orange.game.inventory.Items
import me.orange.game.world.tile.Tiles
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object MineB0t {

    val supervisorJob = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Default + supervisorJob)
    private val logger: Logger = LoggerFactory.getLogger(MineB0t::class.java)
    private lateinit var jda: JDA
    private var stopped = false
    @Volatile private var promptActive = false

    fun start() = runBlocking {
        installPromptAwareOutput()
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

        val timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        logger.info("--------------------------------------------------")
        logger.info("Bot ready at $timeStr. Type 'help' for a list of commands.")
        logger.info("--------------------------------------------------")

        val listenerJob = startCommandListener()
        listenerJob.join()
        stop()
    }

    fun stop() {
        synchronized(this) {
            if (!::jda.isInitialized || stopped) return
            stopped = true

            GamesManager.saveAll()
            jda.shutdown()
            logger.info("Bot stopped")
            supervisorJob.cancel()
        }
    }

    private fun installPromptAwareOutput() {
        val original = System.out
        System.setOut(object : java.io.PrintStream(original, true) {
            override fun write(buf: ByteArray, off: Int, len: Int) {
                // Before the first byte of a log line, erase the stale "> " from the terminal
                if (promptActive) original.print("\r[K")
                super.write(buf, off, len)
                // After a complete line, reprint the prompt
                if (promptActive && len > 0 && buf[off + len - 1] == '\n'.code.toByte()) {
                    original.print("> ")
                    original.flush()
                }
            }
        })
    }

    fun startCommandListener(): Job = scope.launch(Dispatchers.IO) {
        try {
            while (isActive) {
                promptActive = true
                print("> ")
                System.out.flush()
                val command = readlnOrNull() ?: break
                promptActive = false
                ConsoleCommandHandler.dispatch(command)
            }
        } catch (_: Exception) {
            logger.debug("Console reader stream closed.")
        }
    }

    fun launch(block: suspend CoroutineScope.() -> Unit) = scope.launch(block = block)
    fun log(msg: String) = logger.info(msg)
}

