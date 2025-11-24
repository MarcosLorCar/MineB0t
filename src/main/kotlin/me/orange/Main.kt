package me.orange

import kotlinx.coroutines.runBlocking
import me.orange.bot.MineB0t
import me.orange.game.GamesManager

fun saveAll() {
    println("Saving all chunks before shutdown...")
    GamesManager.saveAll()
    println("All chunks saved!")
}

fun main() = runBlocking {
    // Add a shutdown hook
    Runtime.getRuntime().addShutdownHook(Thread {
        saveAll()
    })

    // Launch command listener
    val commandJob = MineB0t.launch {
        while (true) {
            val command = readln()
            when (command) {
                "stop" -> {
                    println("Stopping game...")
                    MineB0t.stop()
                    break
                }
                else -> println("Unknown command: $command")
            }
        }
    }

    MineB0t.start()
    commandJob.join()
}