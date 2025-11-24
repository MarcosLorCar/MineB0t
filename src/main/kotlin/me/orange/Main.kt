package me.orange

import kotlinx.coroutines.runBlocking
import me.orange.bot.MineB0t

fun main() = runBlocking {
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