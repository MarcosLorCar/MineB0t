package me.orange

import me.orange.bot.MineB0t
import me.orange.game.GamesManager

fun saveAll() {
    println("Saving all chunks before shutdown...")
    GamesManager.saveAll()
    println("All chunks saved!")
}

fun main() {
    // Add a shutdown hook
    Runtime.getRuntime().addShutdownHook(Thread {
        saveAll()
    })

    MineB0t.start()
}